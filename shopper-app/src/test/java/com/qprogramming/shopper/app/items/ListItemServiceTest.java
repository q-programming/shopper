package com.qprogramming.shopper.app.items;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.favorites.FavoriteProductsRepository;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private FavoriteProductsRepository favoritesRepositoryMock;

    private ListItemService listItemService;

    @Before
    @Override
    public void setup() {
        super.setup();
        listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock, favoritesRepositoryMock);

    }

    @Test
    public void createListItemTest() throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        ListItem item = TestUtil.createListItem(NAME);
        when(productRepositoryMock.save(any(Product.class))).then(returnsFirstArg());
        ListItem listItem = listItemService.createListItem(item);
        verify(productRepositoryMock, times(1)).save(item.getProduct());
        verify(listItemRepositoryMock, times(1)).save(item);
    }

    @Test(expected = BadProductNameException.class)
    public void createListItemNoProductTest() throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        ListItem item = TestUtil.createListItem(NAME);
        item.setProduct(null);
        listItemService.createListItem(item);
    }

    @Test(expected = BadProductNameException.class)
    public void createListItemBadProductNameTest() throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setName(null);
        listItemService.createListItem(item);
    }

    @Test(expected = ProductNotFoundException.class)
    public void createListItemProductNotFoundTest() throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setId(1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        listItemService.createListItem(item);
    }

    @Test
    public void createListItemProductExistsTest() throws ProductNotFoundException, BadProductNameException, AccountNotFoundException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setId(1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(item.getProduct()));
        listItemService.createListItem(item);
        verify(listItemRepositoryMock, times(1)).save(item);
    }

    @Test
    public void setQuantityFromName() {
        ListItem item = TestUtil.createListItem(NAME);
        item.setQuantity(0f);
        item.getProduct().setName("Name with quantity 2");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(2f);
        assertThat(item.getProduct().getName()).isEqualTo("Name with quantity");

        item.setQuantity(0f);
        item.getProduct().setName("Name with no number 2%");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(0f);

        item.setQuantity(0f);
        item.getProduct().setName("Name with no number");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(0f);

        item.setQuantity(0f);
        item.getProduct().setName("16 items");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(16f);
        assertThat(item.getProduct().getName()).isEqualTo("items");

        item.setQuantity(0f);
        item.getProduct().setName("10 items 16");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(16f);
        assertThat(item.getProduct().getName()).isEqualTo("10 items");

        item.setQuantity(0f);
        item.getProduct().setName("Name");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(0f);

        item.setQuantity(0f);
        item.getProduct().setName("water 1.5");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(1.5f);
        assertThat(item.getProduct().getName()).isEqualTo("water");

        testAccount.setLanguage("pl");
        item.setQuantity(0f);
        item.getProduct().setName("water 1,5");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(1.5f);
        assertThat(item.getProduct().getName()).isEqualTo("water");



    }
}