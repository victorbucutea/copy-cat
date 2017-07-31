using System;
using System.Windows;
using System.Windows.Input;

namespace CopyCat
{
    public class ShowMessageCommand : ICommand
    {
        public void Execute(object parameter)
        {
            MainWindow window = (MainWindow)parameter;
            window.Show();
            window.WindowState = WindowState.Normal;
        }

        public bool CanExecute(object parameter)
        {
            return true;
        }

        public event EventHandler CanExecuteChanged;
    }
}
