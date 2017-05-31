using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Windows;
using System.Windows.Input;

namespace CopyCat
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        List<ClipboardItem> items;


        public MainWindow()
        {
            InitializeComponent();
            MouseDown += Window_MouseDown;

            WindowStyle = WindowStyle.None;
            AllowsTransparency = true;
            ResizeMode = ResizeMode.CanResizeWithGrip;


            items = new List<ClipboardItem>();
            items.Add(new ClipboardItem() { Source = "Mobile", Text = "Some text from remote mobile" , IsChecked = true});
            items.Add(new ClipboardItem() { Source = "Mobile 1", Text = "Some text from remote mobile 1 \nSome text from remote mobile 1" });
            items.Add(new ClipboardItem() { Source = "Mobile 2", Text = "Some text from remote mobile 2" });


            items.Add(new ClipboardItem() { Source = "Mobile 3", Text = "Some text from remote mobile 1 \nSome text from remote mobile 1" });


            items.Add(new ClipboardItem() { Source = "Mobile 4", Text = "Some text from remote mobile 1 \nSome text from remote mobile 1" });
            clipList.ItemsSource = items;
            this.DataContext = items;



        }

        private void Window_MouseDown(object sender, MouseButtonEventArgs e)
        {
            if (e.ChangedButton == MouseButton.Left)
                this.DragMove();
        }

        private void clipList_SelectionChanged(object sender, System.Windows.Controls.SelectionChangedEventArgs e)
        {
            foreach (ClipboardItem clipitem in items)
            {
                clipitem.IsChecked = false;
                clipitem.OnPropertyChanged("IsChecked");
            }

            ClipboardItem item = (ClipboardItem) clipList.SelectedItem;
            item.IsChecked = true;
            item.OnPropertyChanged("IsChecked");
            
        }

        private void Close_Click(object sender, RoutedEventArgs e)
        {
            Application.Current.Shutdown();
        }


    }

    public class ClipboardItem : INotifyPropertyChanged
    {

        public bool IsChecked { get; set; }
        public string Source { get; set; }
        public string Text { get; set; }
        public DateTime Date { get; set; } = new DateTime();

        public event PropertyChangedEventHandler PropertyChanged;

        public virtual void OnPropertyChanged( string IsChecked = null)
        {
            if (PropertyChanged != null)
            {
                PropertyChanged(this, new PropertyChangedEventArgs(IsChecked));
            }
        }
    }
}
