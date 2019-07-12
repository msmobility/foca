pacman::p_load(data.table, dplyr, tidyr, sf, ggplot2, readr)

folders = c("c:/models/freightFlows/output/muc_scenario_zero_c/",
            "c:/models/freightFlows/output/muc_scenario_3km/",
            "c:/models/freightFlows/output/muc_scenario_1km/")

scenarios = c("Base scenario", "Cargo bike - low density", "Cargo bike - high denisty")
selected_DC = 20


# folders = c("c:/models/freightFlows/output/testRegNoCargoBikes/", "c:/models/freightFlows/output/testReg/",
#              "c:/models/freightFlows/output/testReg_2/")
#  
# scenarios = c("base", "cargo-bike", "cargo-bike2")
# selected_DC = 10

summary = data.frame()

scaleFactorTrucks = 1.0
scaleFactorParcels = 1.0

for (i in 1:3){
  
  folder = folders[[i]]
  scenario = scenarios[[i]]
  parcels = fread(paste(folder, "parcels.csv", sep = ""))
  
  ld_trucks = fread(paste(folder, "ld_trucks.csv", sep = ""))
  
  ld_trucks = ld_trucks %>% filter(destinationDistributionCenter == selected_DC)
  
  sd_trucks = fread(paste(folder, "sd_trucks.csv", sep = ""))
  
  vehicle_emissions = fread(paste(folder, "vehicleWarmEmissionFile.csv", sep = ""))
  vehicle_emissions$CO = as.numeric( vehicle_emissions$CO)
  vehicle_emissions$CO2 = as.numeric( vehicle_emissions$CO2)
  vehicle_emissions$HC = as.numeric( vehicle_emissions$HC)
  vehicle_emissions$PM = as.numeric( vehicle_emissions$PM)
  vehicle_emissions$NOx = as.numeric( vehicle_emissions$NOx)

  vehicle_emissions = vehicle_emissions %>% filter(distance != 0)

  ld_trucks_assigned = ld_trucks %>% filter(assigned == T)
  
  trucks_with_emissions = left_join(ld_trucks_assigned, vehicle_emissions, by = "id")
  
  length(unique(parcels %>% filter(distributionType == "CARGO_BIKE") %>% select(destMicroZone))$destMicroZone)
  length(unique(parcels$destMicroZone))
  
  delivered_weight = parcels %>%
    filter(assigned, toDestination, transaction != "PARCEL_SHOP") %>%
    summarize(weight_kg = sum(weight_kg), n = n())
  
  delivered_weight_cargo_bike = parcels %>%
    filter(assigned, toDestination, transaction != "PARCEL_SHOP") %>% group_by(distributionType) %>%
    summarize(weight_kg = sum(weight_kg), n = n())
  
  #write.table(x=trucks_with_emissions, file="clipboard-10000", sep ="\t", row.names = F)
  
  summary_ld_trucks = trucks_with_emissions %>%
    filter(commodity != "NA") %>%
    group_by(commodity) %>%
    summarize(n = n()/scaleFactorTrucks, weight_tn = sum(weight_tn)/scaleFactorTrucks,
              distance = sum(distance)/scaleFactorTrucks, CO2 = sum(CO2)/scaleFactorTrucks,
              NOx = sum(NOx)/scaleFactorTrucks, operatingTime = sum(operatingTime)/scaleFactorTrucks)
  
  summary_vans = vehicle_emissions %>%
    rowwise() %>%
    filter(grepl("van", id)) %>%
    mutate(id = "all") %>% 
    group_by() %>% summarize(n = n()/scaleFactorParcels, distance = sum(distance)/scaleFactorParcels,
                             CO2 = sum(CO2)/scaleFactorParcels, NOx = sum(NOx)/scaleFactorParcels,
                             operatingTime =  sum(operatingTime)/scaleFactorParcels)
  
  summary_vans$commodity = "POST_PACKET"
  summary_vans$weight_tn = delivered_weight$weight_kg[1] / 1000
  
  summary_vans$area = "SD"
  summary_ld_trucks$area = "LD"
  
  summary_cargo_bike = vehicle_emissions %>%
    rowwise() %>%
    filter(grepl("cargoBike", id)) %>%
    mutate(id = "all") %>% 
    group_by() %>% summarize(n = n()/scaleFactorParcels, distance = sum(distance)/scaleFactorParcels,
                             CO2 = sum(CO2)/scaleFactorParcels, NOx = sum(NOx)/scaleFactorParcels,
                             operatingTime =  sum(operatingTime)/scaleFactorParcels)
  summary_cargo_bike$commodity = "POST_PACKET"
  
  if (i == 1){
    summary_cargo_bike$weight_tn = 0
  } else {
    summary_cargo_bike$weight_tn = delivered_weight_cargo_bike$weight_kg[1] / 1000
  }
  
  summary_cargo_bike$area = "SD_Cargo_Bike"
  
  #this_summary = rbind(summary_vans, summary_ld_trucks)
  
  this_summary = rbind(summary_vans, summary_cargo_bike)
  
  this_summary$scenario = scenario
  
  summary = rbind(summary, this_summary)
}

summary_ld_trucks$scenario = "Long distance"

summary = rbind(summary, summary_ld_trucks)

summary = summary %>% filter(commodity == "POST_PACKET")

delivered_weight_cargo_bike$n


summary$parcels = delivered_weight$n

summary$scenario = factor(summary$scenario, levels = c("Long distance", "Base scenario", "Cargo bike - low density", "Cargo bike - high denisty"))


colors_three = c("#d25252","#b06fd4","#1f9214")

ggplot(summary, aes(y=n, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Number of tours") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y=weight_tn, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Sum of weight (tn)")  + 
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y=distance, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Sum of distance (km)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y=distance/weight_tn/1000, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Distance by weight (km/tn)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y=operatingTime/3600/n, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = "stack") +
  ylab("Avg. operating time (h)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y=operatingTime/3600, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = "stack") +
  ylab("Sum of operating time (h)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)


ggplot(summary, aes(y= distance/n/1000, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Avg. distance by vehicle (km)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y= CO2, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("CO2 emissions (kg)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y= CO2/distance, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("CO2 emission by distance (kg/km)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y= CO2/weight_tn/1000, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("CO2 emission by weight (kg/kg)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary, aes(y= NOx/weight_tn/1000, x=commodity, fill = area)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("NOx emission by weight (kg/kg)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)


