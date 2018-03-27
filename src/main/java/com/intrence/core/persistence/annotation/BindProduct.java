/**
 * Created by wliu on 12/5/17.
 */
package com.intrence.core.persistence.annotation;

import com.intrence.core.persistence.common.JsonObject;
import com.intrence.models.model.Product;
import com.intrence.models.model.Size;
import org.skife.jdbi.v2.sqlobject.Binder;
import org.skife.jdbi.v2.sqlobject.BinderFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

@BindingAnnotation(BindProduct.ProductBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface BindProduct {
    class ProductBinderFactory implements BinderFactory {
        @Override
        public Binder build(Annotation annotation) {
            return (Binder<BindProduct, Product>)
                    (q, bind, product) -> {
                        q.bind("uuid", product.getUuid());
                        q.bind("name", product.getName());
                        q.bind("description", product.getDescription());
                        q.bind("designer", product.getDesigner());
                        q.bind("sex", product.getSex() == null ? null : product.getSex().getSexString());
                        q.bind("available_sizes", convertSizeIntoStandards(product.getAvailableSizes()));
                        q.bind("clothing_category", product.getClothingCategory() == null ? null : product.getClothingCategory().getName());
                        q.bind("original_price", new JsonObject<>(product.getOriginalPrice()));
                        q.bind("current_price", new JsonObject<>(product.getCurrentPrice()));
                        q.bind("is_on_sale", product.getIsOnSale());
                        q.bind("sale_discount", product.getSaleDiscount());
                        q.bind("source", product.getSource());
                        q.bind("external_link", product.getExternalLink());
                        q.bind("image_links", product.getImageLinks());
                        q.bind("created_at", product.getCreatedAt());
                        q.bind("updated_at", product.getUpdatedAt());
                    };
        }

        private static Set<String> convertSizeIntoStandards(Set<Size> sizes) {
            Set<String> sizeStandards = new HashSet<>();
            for (Size size : sizes) {
                sizeStandards.add(size.getStandard());
            }
            return sizeStandards;
        }
    }
}
