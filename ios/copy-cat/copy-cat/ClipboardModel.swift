//
//  ClipboardModel.swift
//  copy-cat
//
//  Created by Bucutea Victor on 14/01/2017.
//  Copyright © 2017 Bucutea Victor. All rights reserved.
//

import UIKit

class ClipboardModel : NSObject {
    
    var maxItems: Int = 5
    var items = [ClipboardItem]()
    var closure: () -> Void
    
    override init() {
        closure = {_ in}
    }
    
    init(fromString str: String){
        closure = {_ in}
        let rows = str.components(separatedBy: "{EOR}")
        
        for row in rows {
            let seedStr = row.trimmingCharacters(in: CharacterSet.whitespaces)
            if (!seedStr.isEmpty) {
                items.append(ClipboardItem(seedString: seedStr))
            }
        }
        
    }
    
    
    
    public func add(item: ClipboardItem){
        
        if ( items.contains(item)){
            return
        }
        
        items.insert(item,at: 0)
        
        while ( items.count > maxItems) {
            items.removeLast()
        }
        
        
        closure()
    }
    
    public func get(idx: Int) -> ClipboardItem {
        return items[idx]
    }
    
    public func count() -> Int {
        return items.count
    }
    
    
    public func listenForUpdates(closure: @escaping () -> Void ) {
        self.closure = closure
    }
    
    public func clearSyncStatus(){
        for item in items {
            item.isSynchronized = false
        }
    }
    
    public func toStringForStorage() -> String {
        var result: String = ""
        
        for item in items {
            result.append(item.asStringForStorage())
            result.append("{EOR}")
        }
        
        return result
    }
    
    
    
    
}
