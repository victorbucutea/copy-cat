
using System.Windows.Controls;



namespace CopyCat
{
    /// <summary>
    /// Interaction logic for Dialog.xaml
    /// </summary>
    public partial class Dialog : UserControl
    {

        public string Text { get; set; } = "Sync this item with the rest of your devices ?";

        public bool NoBtnHidden = true;

        public Dialog()
        {
            InitializeComponent();
        }
    }


}
