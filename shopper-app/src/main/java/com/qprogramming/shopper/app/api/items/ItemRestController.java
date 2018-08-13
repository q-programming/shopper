package com.qprogramming.shopper.app.api.items;

import com.qprogramming.shopper.app.items.ListItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Jakub Romaniszyn on 2018-08-13
 */
@RestController
@RequestMapping("/api/item")
public class ItemRestController {

    private static final Logger LOG = LoggerFactory.getLogger(ItemRestController.class);
    private ListItemService listItemService;

    @Autowired
    public ItemRestController(ListItemService listItemService) {
        this.listItemService = listItemService;
    }

}
