package com.qprogramming.shopper.app.api.items.product;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Jakub Romaniszyn on 2018-08-20
 */
public class ProductRestControllerTest extends MockedAccountTestBase {

    public static final String NAME = "name";
    private static final String API_PRODUCT_URL = "/api/product/";
    private static final String API_FIND = "find";
    private static final String API_CATEGORY = "category";
    public static final String NA = "NA";

    @Mock
    private ProductRepository productRepositoryMock;

    private ProductRestController controller;


    @BeforeEach
    void setUp() {
        super.setup();
        controller = new ProductRestController(productRepositoryMock);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }


    @Test
    void getProductsNoTerm() throws Exception {
        Set<Product> products = createProducts(3);
        when(productRepositoryMock.findAll()).thenReturn(new ArrayList<>(products));
        MvcResult mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_FIND)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Product> result = TestUtil.convertJsonToList(contentAsString, List.class, Product.class);
        assertThat(result).containsAll(products);
    }

    @Test
    void getProductsTerm() throws Exception {
        Set<Product> products = createProducts(4);
        when(productRepositoryMock.findByNameContainingIgnoreCase(NA)).thenReturn(products);
        MvcResult mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_FIND).param("term", NA)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Product> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Product.class);
        assertThat(result).containsAll(products);
    }

    @Test
    void getCategoryBasedOnTermTest() throws Exception {
        val product = TestUtil.createProduct(NAME);
        val categoryScore = new HashMap<Category, Long>();
        categoryScore.put(Category.COFFEE_TEA, 1L);
        categoryScore.put(Category.ALCOHOL, 1L);
        categoryScore.put(Category.BAKERY, 3L);
        product.setCategoryScore(categoryScore);
        when(productRepositoryMock.findByNameContainingIgnoreCase(NA)).thenReturn(Collections.singleton(product));
        val mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_CATEGORY).param("term", NA)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        val result = TestUtil.convertJsonToObject(contentAsString, Category.class);
        assertThat(result).isEqualTo(Category.BAKERY);
    }

    @Test
    void getCategoryBasedOnEmptyTermTest() throws Exception {
        when(productRepositoryMock.findByNameContainingIgnoreCase(NA)).thenReturn(Collections.emptySet());
        val mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_CATEGORY).param("term", "")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        val result = TestUtil.convertJsonToObject(contentAsString, Category.class);
        assertThat(result).isEqualTo(Category.OTHER);
    }

    @Test
    void getCategoryBasedOnTermNotFoundTest() throws Exception {
        when(productRepositoryMock.findByNameContainingIgnoreCase(NA)).thenReturn(Collections.emptySet());
        val mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_CATEGORY).param("term", NA)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        val result = TestUtil.convertJsonToObject(contentAsString, Category.class);
        assertThat(result).isEqualTo(Category.OTHER);
    }


    private Set<Product> createProducts(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    Product product = TestUtil.createProduct(NAME + i);
                    product.setId((long) i);
                    return product;
                }).collect(Collectors.toSet());
    }
}