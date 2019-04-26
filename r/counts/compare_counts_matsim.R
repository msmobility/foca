pacman::p_load(data.table, dplyr, ggplot2, reshape2)

#load_tn counts and compare with matsim assignment

##### read assignment results ############################################

workFolder = "c:/models/freightFlows/output/assignmentFull2/"
file_counts = "counts_multi_day.csv" 
matsim_volumes = read.csv(paste(workFolder,file_counts, sep = ""))

matsim_volumes = matsim_volumes %>% select(LINK = link, hour, count)
matsim_volumes$count_matsim = matsim_volumes$count * 20

matsim_volumes = matsim_volumes %>%
  select(ID = LINK,hour, count_matsim)

matsim_volumes$hour = matsim_volumes$hour + 1

#add the >24 hour counts to today counts 

matsim_volumes = matsim_volumes %>% 
  mutate(hour = if_else(hour > 24, hour - 24, hour)) %>% 
  group_by(ID, hour) %>% summarize(count_matsim = sum(count_matsim))



##### read link - station relations ############################################

#links fo the stations
links_stations_folder = "c:/models/freightFlows/working/counts/"
links_stations_file = "matsim_links_stations.csv"
links_stations = fread(paste(links_stations_folder, links_stations_file, sep  = ""))

links_stations = links_stations %>%
  select(ID, LENGTH, Zst = join_stati, distance)

##### merge link - station and matsim###########################################

links_stations = merge(x = links_stations, y = matsim_volumes, by  = "ID")


##### read observed 2010 counts ################################################

source("counts/readAndProcessCountStations_v2.R")

links_stations = merge(links_stations, motorway_counts, by = c("Zst", "hour"))


##### merge links at the same station ##########################################

stations_simulated = links_stations %>%
  mutate(observed_count = H1 + H2) %>%
  group_by(Zst,hour, observed_count) %>%
  summarize(sim_link_count = n() , sim_link_sum = sum(count_matsim))

stations_simulated = stations_simulated %>%
  mutate( count = sim_link_sum/sim_link_count * 2)

#plot simulated vs. obserbed hourly (filter up to 6 links by station - removes interchanges con
# fusing links of trunk and ramps)

ggplot(stations_simulated %>% filter(sim_link_count <= 6),
       aes(x= observed_count, y = count)) + geom_point() + 
  geom_abline(intercept = 0, slope = 1, color = "red" , size = 1) + 
  geom_abline(intercept = 0, slope = .75, color = "red", linetype = "dashed", size = 1) + 
  geom_abline(intercept = 0, slope = 1.25, color = "red", linetype = "dashed", size = 1) + 
  xlab("Osberved (truck/hour)") + 
  ylab("Simulated (truck/hour)") + 
  scale_y_continuous(limits = c(0,1500), expand = c(0,0)) + 
  scale_x_continuous(limits = c(0,1500), expand = c(0,0)) +
  theme_bw() + 
  theme(plot.margin = margin(.5,1,.5, .5, "cm"))

stations_simulated_day = stations_simulated %>%
  filter(sim_link_count <= 6) %>%
  group_by(Zst) %>%
  summarize (observed_count = sum(observed_count), count = sum (count))

ggplot( stations_simulated_day, aes(x= observed_count, y = count)) + geom_point() + 
  geom_abline(intercept = 0, slope = 1, color = "red" , size = 1) + 
  geom_abline(intercept = 0, slope = .75, color = "red", linetype = "dashed", size = 1) + 
  geom_abline(intercept = 0, slope = 1.25, color = "red", linetype = "dashed", size = 1) + 
  xlab("Osberved (truck/day)") + 
  ylab("Simulated (truck/day)") + 
  scale_y_continuous(limits = c(0,20000), expand = c(0,0)) + 
  scale_x_continuous(limits = c(0,20000), expand = c(0,0)) +
  theme_bw() + 
  theme(plot.margin = margin(.5,1,.5, .5, "cm"))


hourly_summary = links_stations %>%
  mutate(observed_count = H1 + H2) %>%
  group_by(hour) %>% 
  summarize(observed_count = sum(observed_count), count_matsim = sum(count_matsim))

hourly_summary$observed_count = hourly_summary$observed_count / sum(hourly_summary$observed_count)
hourly_summary$count_matsim = hourly_summary$count_matsim / sum(hourly_summary$count_matsim)

ggplot(melt(hourly_summary, id.vars = "hour"), aes(x=hour, y = value, color = variable)) + geom_line()
  
#calculate R square and mean error

summary(lm(data = stations_simulated, formula = observed_count ~ count))$r.squared
sqrt(mean((stations_simulated$observed_count - stations_simulated$count)^2))
sqrt(mean((stations_simulated$observed_count - stations_simulated$count)^2))/mean(stations_simulated$observed_count)

summary(lm(data = stations_simulated_day, formula = observed_count ~ count))$r.squared
sqrt(mean((stations_simulated_day$observed_count - stations_simulated_day$count)^2))
sqrt(mean((stations_simulated_day$observed_count - stations_simulated_day$count)^2))/mean(stations_simulated_day$observed_count)

