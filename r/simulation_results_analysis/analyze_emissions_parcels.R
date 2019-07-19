pacman::p_load(data.table, dplyr, tidyr, sf, ggplot2, readr, extrafont)

folders = c("c:/models/freightFlows/output/muc_scenario_zero_c/",
            "c:/models/freightFlows/output/muc_scenario_3km/",
            "c:/models/freightFlows/output/muc_scenario_1km/",
            "c:/models/freightFlows/output/muc_scenario_paketbox/")

scenarios = c("Base (urban)", "a (urban)", "b (urban)","c (urban)" )
selected_DC = 20


# folders = c("c:/models/freightFlows/output/testRegNoCargoBikes/", "c:/models/freightFlows/output/testReg/",
#              "c:/models/freightFlows/output/testReg_2/")
#  
# scenarios = c("base", "cargo-bike", "cargo-bike2")
# selected_DC = 10

summary = data.frame()

scaleFactorTrucks = 1.0
scaleFactorParcels = 1.0

for (i in 1:4){
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
  
  print(
  parcels %>%
    filter(assigned, toDestination, transaction != "PARCEL_SHOP") %>% group_by(distributionType) %>%
    summarize(accessDistance = mean(accessDistance), n = n())
  )
  
  #write.table(x=trucks_with_emissions, file="clipboard-10000", sep ="\t", row.names = F)
  
  summary_ld_trucks = trucks_with_emissions %>%
    filter(commodity != "NA") %>%
    group_by(commodity) %>%
    summarize(n = n()/scaleFactorTrucks, weight_tn = sum(weight_tn)/scaleFactorTrucks,
              distance = sum(distance)/scaleFactorTrucks, CO2 = sum(CO2)/scaleFactorTrucks,
              NOx = sum(NOx)/scaleFactorTrucks, operatingTime = sum(operatingTime)/scaleFactorTrucks)
  
  summary_vans = vehicle_emissions %>%
    rowwise() %>%
    filter(grepl("van", id) | grepl("feeder",id)) %>%
    mutate(id = "all") %>% 
    group_by() %>% summarize(n = n()/scaleFactorParcels, distance = sum(distance)/scaleFactorParcels,
                             CO2 = sum(CO2)/scaleFactorParcels, NOx = sum(NOx)/scaleFactorParcels,
                             operatingTime =  sum(operatingTime)/scaleFactorParcels)
  
  summary_vans$commodity = "POST_PACKET"
  summary_vans$weight_tn = delivered_weight$weight_kg[1] / 1000
  
  summary_vans$vehicle = "Truck"
  summary_ld_trucks$vehicle = "Truck"
  
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
  
  summary_cargo_bike$vehicle = "Cargo bike"
  
  #this_summary = rbind(summary_vans, summary_ld_trucks)
  
  this_summary = rbind(summary_vans, summary_cargo_bike)
  
  this_summary$scenario = scenario
  
  summary = rbind(summary, this_summary)
}

summary_ld_trucks$scenario = "All (inter-urban)"

summary = rbind(summary, summary_ld_trucks)

summary = summary %>% filter(commodity == "POST_PACKET")

delivered_weight_cargo_bike$n


summary$parcels = delivered_weight$n

summary$scenario = factor(summary$scenario, levels = c("All (inter-urban)","Base (urban)", "a (urban)", "b (urban)", "c (urban)"))
summary$vehicle = factor(summary$vehicle, levels = c("Truck", "Cargo bike"))
colors_three = c("#407dd8","#36a332")

ggplot(summary, aes(y=n, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Number of tours") +
  xlab("Scenario (area)") +
  theme_bw() + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = n),position = position_dodge2(width = 1), vjust = -0.5, family = "Times New Roman")


ggplot(summary, aes(y=weight_tn, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Sum of parcel weight (tn)")  + 
  xlab("Scenario (area)") +
  theme_bw() + 
  ylim(0,80) + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = sprintf("%2.1f",weight_tn)),position = position_dodge2(width = 1), vjust = -0.5, family = "Times New Roman")


ggplot(summary, aes(y=distance/1000, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = "stack") +
  ylab("Sum of distance (km)") + 
  xlab("Scenario (area)") +
  ylim(0,8000) + 
  theme_bw() + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = sprintf("%.0f",distance/1000)),position = "stack", vjust = -0.5, family = "Times New Roman")


ggplot(summary, aes(y=distance/weight_tn/1e3, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Distance to deliver 1kg (m/kg)") + 
  xlab("Scenario (area)") +
  theme_bw() + 
  ylim(0,130) + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = sprintf("%2.1f",distance/weight_tn/1e3)),position = position_dodge2(width = 1), vjust = -0.5, family = "Times New Roman")

ggplot(summary, aes(y=operatingTime/3600, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = "stack") +
  ylab("Sum of operating time (h)") + 
  xlab("Scenario (area)") +
  theme_bw() + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = sprintf("%2.1f",operatingTime/3600)),position = "stack", vjust = -0.5, family = "Times New Roman")



ggplot(summary, aes(y= distance/n/1000, x=commodity, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = position_dodge2(preserve = "single")) +
  ylab("Avg. distance by vehicle (km)") +
  xlab("Commodity") + theme(axis.text.x = element_text(angle = 90)) + 
  facet_grid(.~scenario)

ggplot(summary %>% filter(vehicle!="Cargo bike"), aes(y= CO2/weight_tn/1000, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = "stack") +
  ylab("CO2 emission by weight (kg/kg)") + 
  xlab("Scenario (area)") +
  theme_bw() + 
  ylim(0,80) + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = sprintf("%.0f",CO2/weight_tn/1000)),position = "stack", vjust = -0.5, family = "Times New Roman")


ggplot(summary, aes(y= NOx/weight_tn/1000, x=scenario, fill = vehicle)) +
  scale_fill_manual(values = colors_three) + 
  geom_bar(stat = "identity", position = "stack") +
  ylab("NOx emission by weight (kg/kg)") + 
  xlab("Scenario (area)") +
  theme_bw() + 
  theme(text=element_text(size=14, family="Times New Roman"))


counts =data.frame()

for (i in 1:4){
  folder = folders[[i]]
  scenario = scenarios[[i]]
  
  this_counts = read.csv(paste(folder, "matsim/counts.csv", sep =""))
  this_counts$scenario = scenario
  
  counts = rbind(counts, this_counts)
  
}

counts_summary = counts %>% group_by(scenario) %>%
  summarize(van = sum(van), ld = sum(lDTruck), sd = sum(sDTruck), cargoBike = sum(cargoBike))

counts_summary = melt(data = counts_summary, variable.name = "vehicle")


counts_summary$scenario = factor(counts_summary$scenario, levels = c("Base (urban)", "a (urban)", "b (urban)", "c (urban)"))
counts_summary$vehicle = factor(counts_summary$vehicle, levels = c("ld", "sd","van" ,"cargoBike" ), 
                                 labels = c("Truck_LD", "Truck_SD", "Truck", "Cargo bike"))





ggplot(counts_summary %>% filter (vehicle != "Truck_LD", vehicle != "Truck_SD"), aes(y= value, x=scenario, fill = vehicle)) +
  geom_bar(stat = "identity", position = "stack") +
  scale_fill_manual(values = colors_three) + 
  ylab("Sum of link volumes (veh/day)") + 
  xlab("Scenario (area)") +
  theme_bw() + 
  ylim(0,70000) + 
  theme(text=element_text(size=14, family="Times New Roman")) + 
  geom_text(aes(label = value),position = "stack", vjust = -0.5, family = "Times New Roman")





