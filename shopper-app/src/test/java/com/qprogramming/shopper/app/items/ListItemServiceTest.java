package com.qprogramming.shopper.app.items;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.exceptions.BadProductNameException;
import com.qprogramming.shopper.app.exceptions.ProductNotFoundException;
import com.qprogramming.shopper.app.items.favorites.FavoriteProducts;
import com.qprogramming.shopper.app.items.favorites.FavoriteProductsRepository;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cacheMock;

    private ListItemService listItemService;

    @BeforeEach
    void setUp() {
        super.setup();
        when(cacheManager.getCache(anyString())).thenReturn(cacheMock);
        listItemService = new ListItemService(listItemRepositoryMock, productRepositoryMock, favoritesRepositoryMock, cacheManager);

    }

    @Test
    void createListItemTest() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        when(productRepositoryMock.save(any(Product.class))).then(returnsFirstArg());
        ListItem listItem = listItemService.createListItem(item);
        verify(productRepositoryMock, times(1)).save(item.getProduct());
        verify(listItemRepositoryMock, times(1)).save(item);
        verify(cacheMock, times(1)).evict(testAccount.getId());
    }

    @Test
    void createListItemNoProductTest() {
        ListItem item = TestUtil.createListItem(NAME);
        item.setProduct(null);
        assertThrows(BadProductNameException.class, () -> listItemService.createListItem(item));
    }

    @Test
    void createListItemBadProductNameTest() {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setName(null);
        assertThrows(BadProductNameException.class, () -> listItemService.createListItem(item));
    }

    @Test
    void createListItemProductNotFoundTest() {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setId(1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ProductNotFoundException.class, () -> listItemService.createListItem(item));
    }

    @Test
    void createListItemProductExistsTest() throws ProductNotFoundException, BadProductNameException {
        ListItem item = TestUtil.createListItem(NAME);
        item.getProduct().setId(1L);
        FavoriteProducts favorites = new FavoriteProducts();
        favorites.getFavorites().put(item.getProduct(), 1L);
        when(productRepositoryMock.findById(1L)).thenReturn(Optional.of(item.getProduct()));
        when(favoritesRepositoryMock.findById(testAccount.getId())).thenReturn(Optional.of(favorites));
        listItemService.createListItem(item);
        verify(listItemRepositoryMock, times(1)).save(item);
        verify(cacheMock, never()).evict(testAccount.getId());
    }

    @Test
    void getFavoritesSortedTest() {
        Product product1 = TestUtil.createProduct(NAME + 1);
        Product product2 = TestUtil.createProduct(NAME + 2);
        product1.setId(1L);
        product2.setId(2L);
        FavoriteProducts favorites = new FavoriteProducts();
        favorites.getFavorites().put(product1, 1L);
        favorites.getFavorites().put(product2, 2L);
        when(favoritesRepositoryMock.findById(testAccount.getId())).thenReturn(Optional.of(favorites));
        Set<Product> productsForAccount = listItemService.getFavoriteProductsForAccount(testAccount.getId());
        assertThat(productsForAccount.iterator().next()).isEqualTo(product2);
    }

    @Test
    void setQuantityFromName() {
        //string pools
        String name_with_quantity = "Name with quantity";
        String name_with_no_number = "Name with no number";
        String water = "water";
        String potatoes = "potatoes";
        String kg = "kg";

        ListItem item = TestUtil.createListItem(NAME);
        item.setQuantity(0f);
        item.getProduct().setName(name_with_quantity + " 2");
        listItemService.setQuantityFromName(item);
        assertThat(item.getProduct().getName()).isEqualTo(name_with_quantity);
        assertThat(item.getQuantity()).isEqualTo(2f);

        item.setQuantity(0f);
        item.getProduct().setName("Name with no number 2%");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(0f);

        item.setQuantity(0f);
        item.getProduct().setName(name_with_no_number);
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
        item.getProduct().setName(water + " 1.5");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(1.5f);
        assertThat(item.getProduct().getName()).isEqualTo(water);

        testAccount.setLanguage("pl");
        item.setQuantity(0f);
        item.setUnit(null);
        item.getProduct().setName(water + " 1,5");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(1.5f);
        assertThat(item.getProduct().getName()).isEqualTo(water);

        //test with units
        item.setQuantity(0f);
        item.setUnit(null);
        item.getProduct().setName(water + " 1l");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(1.0f);
        assertThat(item.getUnit()).isEqualTo("l");
        assertThat(item.getProduct().getName()).isEqualTo(water);

        item.setQuantity(0f);
        item.setUnit(null);
        item.getProduct().setName(potatoes + " 2,5kg");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(2.5f);
        assertThat(item.getUnit()).isEqualTo(kg);
        assertThat(item.getProduct().getName()).isEqualTo(potatoes);

        item.setQuantity(0f);
        item.getProduct().setName("2,5kg " + potatoes);
        item.setUnit(null);
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isEqualTo(2.5f);
        assertThat(item.getUnit()).isEqualTo(kg);
        assertThat(item.getProduct().getName()).isEqualTo(potatoes);

        item.setQuantity(0f);
        item.setUnit(null);
        item.getProduct().setName(potatoes + " 2,5 kg");
        listItemService.setQuantityFromName(item);
        assertThat(item.getQuantity()).isNotEqualTo(2.5f);
        assertThat(item.getUnit()).isNotEqualTo(kg);
    }
}
