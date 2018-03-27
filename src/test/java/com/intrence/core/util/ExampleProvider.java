/**
 * Created by wliu on 12/7/17.
 */
package com.intrence.core.util;

import com.intrence.models.model.*;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class ExampleProvider {

    public static Product getExampleProduct() {
        return new Product.Builder()
                .uuid(UUID.fromString("81ae3b00-81b4-4cbd-9662-1e5db30a1f7d"))
                .name("Navy Coat")
                .description("Coat Description")
                .designer("Dior")
                .sex(Sex.MEN)
                .availableSizes(new HashSet<>(Arrays.asList(Size.SMALL, Size.MEDIUM, Size.LARGE)))
                .clothingCategory(ClothingCategory.COATS)
                .originalPrice(new Price.Builder().amount(100).currencyCode("USD").formattedAmount("$100.00").build())
                .currentPrice(new Price.Builder().amount(80).currencyCode("USD").formattedAmount("$80.00").build())
                .isOnSale(true)
                .saleDiscount(20)
                .source("farfetch")
                .externalLink("farfetch.com/navy-coat")
                .imageLinks(Arrays.asList("navy-coat.jpg", "navy-coat-2.jpg"))
                .createdAt(DateTime.parse("2018-04-03T04:31:09.128Z"))
                .updatedAt(DateTime.parse("2018-04-03T04:31:09.128Z"))
                .build();
    }
}
