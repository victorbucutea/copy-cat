//
//  TableViewCell.swift
//  copy-cat
//
//  Created by Bucutea Victor on 09/01/2017.
//  Copyright © 2017 Bucutea Victor. All rights reserved.
//

import UIKit

class TableViewCell: UITableViewCell {

    
    @IBOutlet weak var copiedText: UILabel!
    @IBOutlet weak var copiedDescription: UILabel!
    @IBOutlet weak var isSynced: UIImageView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }
    
    func displaySyncedImg(isSync: Bool) {
        isSynced.isHidden = !isSync
    }

}
