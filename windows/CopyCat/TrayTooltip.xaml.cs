using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace CopyCat
{
    /// <summary>
    /// Interaction logic for TrayTooltip.xaml
    /// </summary>
    public partial class TrayTooltip : UserControl
    {
        

        public string Title {get;set;}
        

        public string Message { get; set; }


        public TrayTooltip()
        {
            InitializeComponent();
        }

        public void SetTitle(string newTitle)
        {
            TitleView.Text = newTitle;
        }



        public void SetMessage(string newTitle)
        {
            MessageView.Text = newTitle;
        }
    }
}
