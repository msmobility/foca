#install.packages("opencage")
#need to add the api key as the environment variable OPENCAGE_KEY

pacman::p_load(data.table, ggplot2, dplyr, reshape, opencage)

folder = "c:/models/freightFlows/"

source("input/input_zones.R")

external_zones = zones %>% filter(type == "ext")

external_zones$lat = 0
external_zones$lon = 0

for (row in 1:nrow(external_zones)){
  this_zone_name = as.character(external_zones$Verkehrszellenname[row])
  this_zone_name = strsplit(this_zone_name[[1]],"/")[[1]][1]
  this_zone_name = strsplit(this_zone_name[[1]]," ")[[1]][1]
  output <- opencage_forward(placename = this_zone_name, language = "de")
  print(paste(this_zone_name, output$results$components.country_code[1], sep = " - "))
  if(length(output$results)> 0){
    external_zones$lat[row]= output$results$geometry.lat[1]
    external_zones$lon[row] = output$results$geometry.lng[1]
  }
}

write.csv(external_zones, paste(folder,"input/exteralZonesCoord.csv", sep = ""), row.names = F)
