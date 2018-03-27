/**
 * Created by wliu on 12/7/17.
 */
package com.intrence.core.persistence.dao;

import com.google.inject.Guice;
import com.intrence.core.modules.PostgresModule;
import com.intrence.models.model.Product;
import com.intrence.core.util.ExampleProvider;
import org.junit.Test;

public class ProductDaoTest {

    private static Product product = ExampleProvider.getExampleProduct();
    private static ProductDao productDao = Guice.createInjector(new PostgresModule()).getInstance(ProductDao.class);

//    @Test
    public void testCreateProduct() {
        productDao.createProduct(product);
    }

//    @Test
    public void testReadProduct() throws Exception {
        Product productFromQuery = productDao.getProductById(product.getUuid());
        System.out.println(productFromQuery.toJson());
    }

//    @Test
    public void testUpdateProduct() {

    }

//    @Test
    public void testDeleteProduct() {

    }

//    @Test
    public void testUpsertProduct() {
        productDao.upsertProduct(product);
    }

}
