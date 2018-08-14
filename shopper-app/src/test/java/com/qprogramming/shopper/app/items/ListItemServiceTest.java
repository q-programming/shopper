package com.qprogramming.shopper.app.items;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by Jakub Romaniszyn on 2018-08-13
 */
public class ListItemServiceTest extends MockedAccountTestBase {

    public static final String NAME = "name";
    @Mock
    private ProductRepository productRepositoryMock;
    @Mock
    private ListItemRepository listItemRepositoryMock;

    private ListItemService listItemService;

    @Before
    @Override
    public void setup() {
        super.setup();
        listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock);

    }

    @Test
    public void createListItem() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        when(productRepositoryMock.save(any(Product.class))).then(returnsFirstArg());
        ListItem listItem = listItemService.createListItem(item);
        verify(productRepositoryMock, times(2)).save(item.getProduct());
        verify(listItemRepositoryMock, times(1)).save(item);
    }

    @Test(expected = BadProductNameException.class)
    public void createListItemNoProduct() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        item.setProduct(null);
        listItemService.createListItem(item);
    }

    @Test(expected = BadProductNameException.class)
    public void createListItemBadProductName() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setName(null);
        listItemService.createListItem(item);
    }

    @Test(expected = ProductNotFoundException.class)
    public void createListItemProductNotFound() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setId(1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        listItemService.createListItem(item);
    }

    @Test
    public void createListItemProductExists() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setId(1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(item.getProduct()));
        listItemService.createListItem(item);
        verify(listItemRepositoryMock, times(1)).save(item);
    }
}