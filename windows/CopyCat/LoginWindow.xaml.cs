

using System;
using System.ComponentModel;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Input;

namespace CopyCat
{
   
    public partial class LoginWindow : Window
    {
        // FacebookClient client;
        //The Application ID from Facebook
        public string AppID { get; set; }

        //The access token retrieved from facebook's authentication
        public string AccessToken { get; set; }

        public LoginWindow()
        {
            InitializeComponent();
            this.Loaded += (object sender, RoutedEventArgs e) =>
            {
                //Add the message hook in the code behind since I got a weird bug when trying to do it in the XAML
                webBrowser.MessageHook += webBrowser_MessageHook;

                //Delete the cookies since the last authentication
                //DeleteFacebookCookie();

                //Create the destination URL
                var destinationURL = string.Format("https://www.facebook.com/dialog/oauth?client_id={0}&scope={1}&display=popup&redirect_uri={2}&response_type=token",
                   AppID, //client_id
                   "email", //scope
                   "http://localhost"
                );
                webBrowser.Navigate(destinationURL);
            };
        }

        private void webBrowser_Navigated(object sender, System.Windows.Navigation.NavigationEventArgs e)
        {
            //If the URL has an access_token, grab it and walk away...
            var url = e.Uri.Fragment;
            if (url.Contains("access_token") && url.Contains("#"))
            {
                url = (new System.Text.RegularExpressions.Regex("#")).Replace(url, "?", 1);
                AccessToken = System.Web.HttpUtility.ParseQueryString(url).Get("access_token");
                DialogResult = true;
                this.Close();
            }
        }



        private void DeleteFacebookCookie()
        {
            //Set the current user cookie to have expired yesterday
            string cookie = string.Format("c_user=; expires={0:R}; path=/; domain=.facebook.com", DateTime.UtcNow.AddDays(-1).ToString("R"));
            Application.SetCookie(new Uri("https://www.facebook.com"), cookie);
        }

        private void webBrowser_Navigating(object sender, System.Windows.Navigation.NavigatingCancelEventArgs e)
        {
            if (e.Uri.LocalPath == "/r.php")
            {
                MessageBox.Show("To create a new account go to www.facebook.com", "Could Not Create Account", MessageBoxButton.OK, MessageBoxImage.Error);
                e.Cancel = true;
            }
        }

        IntPtr webBrowser_MessageHook(IntPtr hwnd, int msg, IntPtr wParam, IntPtr lParam, ref bool handled)
        {
            //msg = 130 is the last call for when the window gets closed on a window.close() in javascript

            Console.WriteLine("message "+msg + " param " + lParam);
            if (msg == 130)
            {
                this.Close();
            }
            return IntPtr.Zero;
        }


    }
}
