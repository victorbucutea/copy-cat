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
using Hardcodet.Wpf.TaskbarNotification;

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
            items.Add(new ClipboardItem() { Source = "Mobile", Text = "Some text from remote mobile" });
            clipList.ItemsSource = items;
            DataContext = items;
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
                var item = (ClipboardItem)clipList.SelectedItem;
                item.IsChecked = true;
                item.OnPropertyChanged("IsChecked");
                socket.Emit("message", item.Source, item.Text);

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
            socket.Close();
            System.Windows.Application.Current.Shutdown();
        }

        private void Window_ContentRendered(object sender, EventArgs e)
        {
            //5 Monitor clipboard 
            //6 on click selected item re-emit message

            manager = new Manager(new Uri("http://localhost:3000"));
            socket = socket = manager.Socket("/mychannel");
            socket.On(Socket.EVENT_CONNECT, () =>
            {
                Console.WriteLine("EVENT_CONNECT");
                App.Current.Dispatcher.Invoke(delegate
                {
                    StatusIndicator.Background = new SolidColorBrush(Color.FromRgb(60, 118, 61));
                    StatusIndicator.Content = "Connected";
                    StatusIndicator.ToolTip = "Ready to send/receive";
                });

                socket.On("message", new ListenerImpl2Params((msg1, msg2) =>
                {
                    App.Current.Dispatcher.Invoke(delegate
                    {
                        String text = (String)msg2;
                        String src = (String)msg1;
                        String baloonText = text;
                        if (text.Length > 30)
                        {
                            baloonText = baloonText.Substring(0, 30) + "...";
                        }
                        items.Add(new ClipboardItem() { Source = src, Text = text, IsChecked = true });
                        tbIcon.ShowBalloonTip("New Message", baloonText, BalloonIcon.Info);
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
