pacman::p_load(data.table, dplyr, ggplot2)

#read station data

input_folder = "c:/models/freightFlows/working/counts/input/"

file_motorway = "2010_A_S.txt"

motorway_counts = fread(paste(input_folder, file_motorway, sep = ""))

motorway_counts = motorway_counts %>%
  group_by(Zst, Land, hour = Stunde) %>% 
  summarize(L1 = mean(KFZ_R1), L2 = mean(KFZ_R2), H1 = mean(Lkw_R1), H2 = mean(Lkw_R2))

#stop if go to analyzeHourlyData

# motorway_counts = motorway_counts %>%
  # group_by(Zst, Land) %>% summarize_all(sum) 


