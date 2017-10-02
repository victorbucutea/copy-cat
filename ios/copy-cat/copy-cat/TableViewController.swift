//
//  TableViewController.swift
//  copy-cat
//
//  Created by Bucutea Victor on 09/01/2017.
//  Copyright © 2017 Bucutea Victor. All rights reserved.
//

import UIKit
import SocketIO

class TableViewController: UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    var clipboardModel: ClipboardModel = ClipboardModel();
    
    @IBOutlet weak var tableView: UITableView!
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        tableView.delegate = self
        tableView.dataSource = self
        initClipboardModel()
        let socket = SocketIOClient(socketURL: URL(string: "http://localhost:3000")!)
        socket.connect()
        
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // MARK: - Table view data source
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return clipboardModel.count()
    }

    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cellIdentifier = "TableViewCell"
        
        guard let cell = tableView.dequeueReusableCell(withIdentifier: cellIdentifier, for: indexPath) as? TableViewCell  else {
            fatalError("The dequeued cell is not an instance of TableViewCell.")
        }

        let clipItem = clipboardModel.get(idx: indexPath.row)
        
        // Configure the cell...
        cell.copiedText.text = clipItem.text
        cell.copiedDescription.text = clipItem.description
        cell.displaySyncedImg(isSync: clipItem.isSynchronized)

        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let alertController = UIAlertController(title: "Synchronize", message: "Make item available for paste on all your devices?", preferredStyle: .alert)
        
        let confirmAction = UIAlertAction(title: "Confirm", style: .default) { (_) in
            self.clipboardModel.clearSyncStatus()
            let item = self.clipboardModel.get(idx: indexPath.row)
            item.isSynchronized = true
            self.tableView.reloadData()
        }
        
        let cancelAction = UIAlertAction(title: "Cancel", style: .cancel) { (_) in }
        
        
        alertController.addAction(confirmAction)
        alertController.addAction(cancelAction)
        
        
        self.present(alertController, animated: true, completion: nil)
    }
 

    /*
    // Override to support conditional editing of the table view.
    override func tableView(_ tableView: UITableView, canEditRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the specified item to be editable.
        return true
    }
    */

    /*
    // Override to support editing the table view.
    override func tableView(_ tableView: UITableView, commit editingStyle: UITableViewCellEditingStyle, forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            // Delete the row from the data source
            tableView.deleteRows(at: [indexPath], with: .fade)
        } else if editingStyle == .insert {
            // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
        }    
    }
    */

    /*
    // Override to support rearranging the table view.
    override func tableView(_ tableView: UITableView, moveRowAt fromIndexPath: IndexPath, to: IndexPath) {

    }
    */

    /*
    // Override to support conditional rearranging of the table view.
    override func tableView(_ tableView: UITableView, canMoveRowAt indexPath: IndexPath) -> Bool {
        // Return false if you do not want the item to be re-orderable.
        return true
    }
    */

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

    
    // MARK: Private methods
    private func initClipboardModel() {
        let appDelegate = UIApplication.shared.delegate as! AppDelegate
        clipboardModel = appDelegate.clipboardModel
        clipboardModel.listenForUpdates {
            let state: UIApplicationState = UIApplication.shared.applicationState
            
            if (state == .background) {
                return
            }
            
            self.tableView.reloadData()
        }
    }

    
}
