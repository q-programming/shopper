package com.qprogramming.shopper.app.api.items.product;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.items.product.ProductRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    public static final String NA = "NA";

    @Mock
    private ProductRepository productRepositoryMock;

    private ProductRestController controller;


    @Before
    @Override
    public void setup() {
        super.setup();
        controller = new ProductRestController(productRepositoryMock);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }


    @Test
    public void getProductsNoTerm() throws Exception {
        Set<Product> products = createProducts(3);
        when(productRepositoryMock.findAll()).thenReturn(new ArrayList<>(products));
        MvcResult mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_FIND)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        List<Product> result = TestUtil.convertJsonToList(contentAsString, List.class, Product.class);
        assertThat(result).containsAll(products);
    }

    @Test
    public void getProductsTerm() throws Exception {
        Set<Product> products = createProducts(4);
        when(productRepositoryMock.findByNameContainingIgnoreCase(NA)).thenReturn(products);
        MvcResult mvcResult = this.mvc.perform(get(API_PRODUCT_URL + API_FIND).param("term", NA)
                .contentType(TestUtil.APPLICATION_JSON_UTF8)).andExpect(status().is2xxSuccessful()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Set<Product> result = TestUtil.convertJsonToSet(contentAsString, Set.class, Product.class);
        assertThat(result).containsAll(products);
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