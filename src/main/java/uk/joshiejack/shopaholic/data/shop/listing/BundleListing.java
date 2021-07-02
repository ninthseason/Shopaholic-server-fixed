package uk.joshiejack.shopaholic.data.shop.listing;

import uk.joshiejack.penguinlib.data.database.CSVUtils;
import uk.joshiejack.shopaholic.data.ShopaholicDatabase;

import java.util.ArrayList;
import java.util.List;

public class BundleListing extends SublistingBuilder {
    private final List<SublistingBuilder> builders = new ArrayList<>();

    public BundleListing(String bundleID) {
        super("bundle", bundleID);
    }

    public BundleListing addToBundle(SublistingBuilder builder) {
        this.builders.add(builder);
        return this;
    }

    @Override
    public void save(ShopaholicDatabase data) {
        builders.forEach(sublisting -> {
            data.addEntry("bundles", "ID,Type,Data", CSVUtils.join(this.data, sublisting.type, sublisting.data));
            sublisting.save(data);
        });
    }
}
