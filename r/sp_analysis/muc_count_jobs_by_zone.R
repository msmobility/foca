pacman::p_load(data.table, dplyr, tidyr, sf, ggplot2)

#read the microdata of jobs

folder_sp = "c:/models/silo/muc/microData/"

jj = fread(paste(folder_sp, "jj_2011.csv", sep = ""))

jobs_by_zone = jj %>% 
  group_by(type, zone) %>% 
  summarise(count = n()) %>%
  spread(type, count, fill = 0)


write.table(jobs_by_zone, "clipboard-1000", sep = "\t", row.names = F)

unique(jj$type)


#and attach it to the zone shapefile

folder_shp = "c:/models/freightFlows/input/shp/"


original_file = paste(folder_shp, "zones_4326.shp", sep="")

data = st_read(original_file)

data = merge(x=data, y=jobs_by_zone, by.x = "id", by.y = "zone", all.x = T)

data[is.na(data)] = 0


final_file = paste(folder_shp, "zones_4326_jobs.shp", sep="")

sf::write_sf(data, final_file)

munich_data = data %>% filter(AGS == 9162000)

employment_types = unique(jj$type)

for (type in employment_types){
  index_this_type = match(type, names(munich_data))
  plot_data = data.frame(id = munich_data$id,
                         value = munich_data[,index_this_type][[1]],
                         area = munich_data$Area,
                         geometry = munich_data$geometry)
  plot = ggplot(data = plot_data, aes(fill = value/area)) + 
    geom_sf(color = NA) +
    scale_fill_gradient(low = "white", high = "red") + 
    coord_sf() + ggtitle(type)
  print(plot)
}


