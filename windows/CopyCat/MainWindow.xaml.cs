using MaterialDesignThemes.Wpf;
using System;
using System.ComponentModel;
using System.Windows;
using System.Windows.Input;
using System.Windows.Media;
using Quobject.EngineIoClientDotNet.ComponentEmitter;
using Quobject.SocketIoClientDotNet.Client;
using Socket = Quobject.SocketIoClientDotNet.Client.Socket;
using System.Collections.ObjectModel;
using Facebook;
using System.Dynamic;
using System.Net.Http;
using System.Threading.Tasks;
using Newtonsoft.Json;
using System.Text;

namespace CopyCat
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        ObservableCollection<ClipboardItem> items;
        Manager manager;
        Socket socket;


        public MainWindow()
        {
            InitializeComponent();
            MouseDown += Window_MouseDown;

            WindowStyle = WindowStyle.None;
            AllowsTransparency = true;
            ResizeMode = ResizeMode.CanResizeWithGrip;

            items = new ObservableCollection<ClipboardItem>();
            clipList.ItemsSource = items;
            DataContext = items;

            ShowLogin();
        }
        


        private void Window_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                this.DragMove();
        }

        private async void clipList_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
        {
            var view = new Dialog();
            var result = await DialogHost.Show(view);

            if ("YES".Equals(result))
            {
                var item = (ClipboardItem) clipList.SelectedItem;
                item.IsChecked = true;
                item.OnPropertyChanged("IsChecked");
                socket.Emit("message", item.Text, item.Source);

                foreach (ClipboardItem clipitem in items)
                {
                    if (clipitem != item)
                    {
                        clipitem.IsChecked = false;
                        clipitem.OnPropertyChanged("IsChecked");
                    }
                }
            }
        }

        private void Click_Close(object sender, RoutedEventArgs e)
        {
            this.WindowState = WindowState.Minimized;
            this.Hide();
        }

        private void Click_Exit(object sender, RoutedEventArgs e)
        {
            if (socket != null)
            {
                socket.Close();
            }
            System.Windows.Application.Current.Shutdown();
        }

        private void Window_ContentRendered(object sender, EventArgs e)
        {
            initClipboardListener();
        }

        private void ShowLogin()
        {
            LoginWindow dialog = new LoginWindow() { AppID = "820943364752707" };
            if (dialog.ShowDialog() == true)
            {
                string accessToken = dialog.AccessToken;
                var fb = new FacebookClient(accessToken);
                dynamic parameters = new ExpandoObject();
                parameters.fields = "id,name,email";
                dynamic me = fb.Get("me", parameters);
                var email = me.email;
                if (email != null)
                {
                    email = email.Replace("@", "_");
                    initSocketIo(email);
                }
            } else
            {
                Application.Current.Shutdown(); 
            }
        }

        private void initClipboardListener()
        {
            var windowClipboardManager = new ClipboardManager(this);
            windowClipboardManager.ClipboardChanged += ClipboardChanged;
        }

        private void ClipboardChanged(object sender, EventArgs e)
        {
            // Handle your clipboard update here, debug logging example:
            if (Clipboard.ContainsText())
            {
                AddItem(new ClipboardItem() { IsChecked = true, Source = "Windows", Text = Clipboard.GetText() });
            }
        }

        private void AddItem(ClipboardItem item)
        {
            items.Insert(0,item);
            socket.Emit("message", Clipboard.GetText(), "Windows");
            foreach (ClipboardItem clipitem in items)
            {
                if (clipitem != item)
                {
                    clipitem.IsChecked = false;
                    clipitem.OnPropertyChanged("IsChecked");
                }
            }

            if(items.Count > 0)
            {
                NoClipMsg.Visibility = Visibility.Hidden;
            }

            if(items.Count > 5)
            {
                items.RemoveAt(items.Count - 1);
            }
        }


        void CreateChannel(string channelName)
        {
            try
            {
                HttpClient client = new HttpClient();
                var req = new 
                {
                    id= channelName
                };
                StringContent content = new StringContent(JsonConvert.SerializeObject(req).ToString(), Encoding.UTF8, "application/json");
                Task<HttpResponseMessage> task = client.PostAsync("http://localhost:3000/channel", content);
                task.Wait();
                task.Result.EnsureSuccessStatusCode();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error contacting the server. Check internet connection or try again later.","Error",
                    MessageBoxButton.OK,MessageBoxImage.Error);
                Application.Current.Shutdown();
            }
            //TODO status Disconnected (e.g. server shutdown) should become connecting after some time
        }

        private  void initSocketIo(string channel)
        {
            CreateChannel(channel);

            manager = new Manager(new Uri("http://localhost:3000"));
            socket = socket = manager.Socket("/"+channel);
            socket.On(Socket.EVENT_CONNECT, () =>
            {
                Console.WriteLine("EVENT_CONNECT");
                App.Current.Dispatcher.Invoke(delegate
                {
                    StatusIndicator.Background = new SolidColorBrush(Color.FromRgb(60, 118, 61));
                    StatusIndicator.Content = "Connected";
                    StatusIndicator.ToolTip = "Ready to send/receive clipboard content";
                });

                socket.On("message", new ListenerImpl2Params((msg1, msg2) =>
                {
                    App.Current.Dispatcher.Invoke(delegate
                    {
                        String text = (String)msg1;
                        String src = (String)msg2;
                        String baloonText = text;
                        if (text.Length > 30)
                        {
                            baloonText = baloonText.Substring(0, 30) + "...";
                        }

                        AddItem(new ClipboardItem() { Source = src, Text = text, IsChecked = true });
                    });
                }));
            });

            socket.On(Socket.EVENT_DISCONNECT, (data) =>
            {
                Console.WriteLine("EVENT_DISCONNECT");
                App.Current.Dispatcher.Invoke(delegate
                {
                    StatusIndicator.Background = new SolidColorBrush(Color.FromRgb(162, 28, 28));
                    StatusIndicator.Content = "Disconnected";
                    StatusIndicator.ToolTip = "Server has closed connection. Reconnecting ...";
                });
            });



            socket.On(Socket.EVENT_ERROR, (data) =>
            {
                Console.WriteLine("EVENT_ERROR");
                App.Current.Dispatcher.Invoke(delegate
                {
                    StatusIndicator.Background = new SolidColorBrush(Color.FromRgb(162, 28, 28));
                    StatusIndicator.Content = "Error";
                    StatusIndicator.ToolTip = "Error establishing connection. Reconnecting ...";
                });
            });
        }
    }



    public class ClipboardItem : INotifyPropertyChanged
    {

        public bool IsChecked { get; set; }
        public string Source { get; set; }
        public string Text { get; set; }
        public DateTime Date { get; set; } = DateTime.Now;

        public event PropertyChangedEventHandler PropertyChanged;

        public virtual void OnPropertyChanged(string IsChecked = null)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(IsChecked));
            }
        }
    }

    public class ListenerImpl2Params : IListener
    {
        private static int id_counter = 0;
        private int Id;
        private readonly Action<object, object> fn;

        public ListenerImpl2Params(Action<object, object> fn)
        {
            this.fn = fn;
            this.Id = id_counter++;
        }

        public void Call(params object[] args)
        {
            if (fn != null)
            {
                var arg0 = args.Length > 0 ? args[0] : null;
                var arg1 = args.Length > 1 ? args[1] : null;
                fn(arg0, arg1);
            }
        }



        public int CompareTo(IListener other)
        {
            return this.GetId().CompareTo(other.GetId());
        }

        public int GetId()
        {
            return Id;
        }
    }
}
