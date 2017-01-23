//
//  ClipboardItem.swift
//  copy-cat
//
//  Created by Bucutea Victor on 09/01/2017.
//  Copyright © 2017 Bucutea Victor. All rights reserved.
//

import UIKit

class ClipboardItem: Hashable {

    var text: String
    var description: String
    var creationDate: Date
    var dateFormat: String = "dd/MM/yyyy HH:mm"
    var isSynchronized: Bool = false

    
    init(text: String, source: String?) {
        creationDate = Date()
        self.text = text
        description = (source)!
    }
    
    init(seedString: String){
        let formatter = DateFormatter()
        formatter.dateFormat = dateFormat
        var comps = seedString.components(separatedBy: "{FVAL}")
        self.text = comps[0]
        self.description = comps[1]
        self.creationDate = formatter.date(from: comps[2])!
        
        if ( comps.count > 3 ) {
            self.isSynchronized = Bool(comps[3])!
        }
    }
    
    var hashValue: Int {
        return text.hashValue
    }
    
    
    
    public func asStringForStorage() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = dateFormat
        return text + "{FVAL}" + description
            + "{FVAL}" + formatter.string(from: creationDate)
            + "{FVAL}" + isSynchronized.description
    }
    
    
    static func == (lhs: ClipboardItem, rhs: ClipboardItem) -> Bool {
        return lhs.text == rhs.text
    }
    

}
