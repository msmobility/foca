pacman::p_load(data.table, dplyr)

path = "c:/models/freightFlows/output/assignmentFull/truckFlows.csv"

aux = fread(path)
aux = aux %>% filter(trucks >0)

fwrite(aux, "c:/models/freightFlows/output/assignmentFull/truckFlows_v2.csv", row.names= F )


aux = aux %>% rowwise() %>%
  mutate(tt_h = max(round(tt/3600, digits = 0),1))

aux = aux %>% group_by(tt_h) %>% summarize(trucks = sum(trucks))


fwrite(aux, "c:/models/freightFlows/output/assignmentFull/truckFlows_v3.csv", row.names= F )


#aggregate_by_distance

distance_bins = c(0,50,100,200,500,10000)
aux = aux %>% mutate(bin = cut(distanceBin, distance_bins))

aux = aux %>% group_by(bin) %>% summarize(sum(volume_tn))


#aggregate+by_commodity

aux = aux %>% group_by(commodity) %>% summarize(sum(volume_tn))
