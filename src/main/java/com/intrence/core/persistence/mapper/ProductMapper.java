/**
 * Created by wliu on 12/1/17.
 */
package com.intrence.core.persistence.mapper;

import com.intrence.models.model.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductMapper implements ResultSetMapper<Product> {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @Override
    public Product map(int index, ResultSet resultSet, StatementContext ctx) throws SQLException {
        return new Product.Builder()
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .designer(resultSet.getString("designer"))
                .sex(resultSet.getString("sex") == null ? null : Sex.fromSexString(resultSet.getString("sex")))
                .availableSizes(mapAvailableSizesArrayToSet(resultSet.getArray("available_sizes")))
                .clothingCategory(resultSet.getString("clothing_category") == null ? null : ClothingCategory.fromClothingCategoryName(resultSet.getString("clothing_category")))
                .originalPrice(Price.fromJson(resultSet.getString("original_price")))
                .currentPrice(Price.fromJson(resultSet.getString("current_price")))
                .isOnSale(resultSet.getBoolean("is_on_sale"))
                .saleDiscount(resultSet.getInt("sale_discount"))
                .source(resultSet.getString("source"))
                .externalLink(resultSet.getString("external_link"))
                .imageLinks(mapImageLinksArrayToList(resultSet.getArray("image_links")))
                .createdAt(mapDateTime("created_at", resultSet, ctx))
                .updatedAt(mapDateTime("updated_at", resultSet, ctx))
                .build();
    }

    private static Set<Size> mapAvailableSizesArrayToSet(Array availableSizes) throws SQLException {
        Set<Size> availableSizeSet = new HashSet<>();
        String[] availableSizesArray = (String[]) availableSizes.getArray();
        for (String availableSize : availableSizesArray) {
            availableSizeSet.add(Size.fromSizeStandard(availableSize));
        }
        return availableSizeSet;
    }

    private static List<String> mapImageLinksArrayToList(Array imageLinks) throws SQLException {
        String[] imageLinksArray = (String[]) imageLinks.getArray();
        return Arrays.asList(imageLinksArray);
    }

    private DateTime mapDateTime(String column, ResultSet resultSet, StatementContext ctx) throws SQLException {
        return (DateTime) ctx.columnMapperFor(DateTime.class).mapColumn(resultSet, column, ctx);
    }

}
