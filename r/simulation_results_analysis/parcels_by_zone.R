pacman::p_load(data.table, dplyr, tidyr, sf, ggplot2)


path_parcels = "C:/models/freightFlows/output/results_2010/parcels.csv"

parcels_2010 = fread(path_parcels)

path_dc = "C:/models/freightFlows/input/distributionCenters/distributionCenters.csv"

distribution_centers = fread(path_dc)


path_muc_shp = "C:/models/freightFlows/input/shp/zones_4326.shp"

zones_muc = st_read(path_muc_shp)


path_reg_shp = "C:/models/freightFlows/input/shp/zones_regensburg_4326.shp"

zones_reg = st_read(path_reg_shp)


#deliveries at destination, by distribution center, munich: 
deliveries = parcels_2010 %>% filter(toDestination, transaction != "WAREHOUSE", transaction != "PARCEL_SHOP")


summary = deliveries %>% group_by(destMicroZone, transaction) %>%
  summarize (count = n()) %>%
  spread(transaction, count)

summary[is.na(summary)] = 0

zones_muc = merge(x = zones_muc, y = summary, by.x = "id", by.y = "destMicroZone")

new_muc_shp_path = "C:/models/freightFlows/working/output_analyses/muc_zones_4326_with_parcels.shp"
st_write(zones_muc, new_muc_shp_path)


zones_reg = merge(x = zones_reg, y = summary, by.x = "id", by.y = "destMicroZone")


new_reg_shp_path = "C:/models/freightFlows/working/output_analyses/reg_zones_4326_with_parcels.shp"
st_write(zones_reg, new_reg_shp_path)

#analyze parcel weight

ggplot(parcels_2010, aes(x=weight_kg)) + stat_ecdf()
