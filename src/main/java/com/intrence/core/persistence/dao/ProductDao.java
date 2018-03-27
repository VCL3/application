/**
 * Created by wliu on 11/30/17.
 */
package com.intrence.core.persistence.dao;

import com.intrence.core.persistence.annotation.BindProduct;
import com.intrence.models.model.Product;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;

import java.util.UUID;

public interface ProductDao {

    @SqlQuery("SELECT exists (SELECT 1 FROM products WHERE uuid = :uuid LIMIT 1)")
    boolean lookupProductById(@Bind("uuid") UUID productId);

    @SqlUpdate("INSERT INTO products (uuid, name, description, designer, sex, available_sizes, clothing_category, original_price, current_price, is_on_sale, sale_discount, source, external_link, image_links, created_at, updated_at) VALUES (:uuid, :name, :description, :designer, :sex::sex_enum, :available_sizes::clothing_size_enum[], :clothing_category, :original_price, :current_price, :is_on_sale, :sale_discount, :source, :external_link, :image_links, :created_at, :updated_at)")
    void createProduct(@BindProduct Product product);

    @SqlQuery("SELECT * FROM products WHERE uuid = :uuid")
    Product getProductById(@Bind("uuid") UUID productId);

    @SqlQuery("SELECT source FROM products WHERE uuid = :uuid")
    String getSourceById(@Bind("uuid") UUID productId);

    @SqlUpdate("UPDATE products SET name = :name, description = :description WHERE uuid = :uuid")
    void updateProduct(@BindProduct Product product);

    @SqlUpdate("DELETE FROM products where uuid = :uuid")
    void deleteProductById(@Bind("uuid") UUID productId);

    @Transaction
    default void upsertProduct(final Product product) {
        if (!lookupProductById(product.getUuid())) {
            // insert new voucher details tracking
            createProduct(product);
        } else {
            // update existing voucher details tracking
            updateProduct(product);
        }
    }
}
