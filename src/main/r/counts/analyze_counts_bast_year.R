pacman::p_load(data.table, dplyr, ggplot2)

#read station data

input_folder = "c:/models/freightFlows/working/counts/input/"

file_motorway = "2010_A_S.txt"

motorway_counts = fread(paste(input_folder, file_motorway, sep = ""))





stations = unique(motorway_counts$Zst)

motorway_counts$m_d = motorway_counts$Datum - 100000
motorway_counts$month = round(motorway_counts$m_d/100)

summary(motorway_counts$month)



summary = motorway_counts %>% group_by(Wotag, month) %>% summarize(count = 24 * (mean(Lkw_R1) + mean(Lkw_R2)))


ggplot(summary, aes(x=as.factor(Wotag), y = count, color = as.factor(month), group = month)) +
  geom_line(size = 1) + 
  xlab("Day of week") + ylab("Sum of counts")

ggplot(summary, aes(x=month, y = count, color = as.factor(Wotag), group = Wotag)) +
  geom_path(size = 1) + 
  geom_point() + 
  xlab("Month") + ylab("AADT_t") 


summary = motorway_counts %>%
  group_by(month)  %>%
  summarize(count = 24 * (mean(Lkw_R1) + mean(Lkw_R2)))

ggplot(summary, aes(x=month, y = count)) +
  geom_path(size = 1) + 
  geom_point() + 
  xlab("Month") + ylab("AADT_t") 





daily_summary = motorway_counts %>% group_by(Datum) %>% 
  summarize(count = 1/length(stations) * (sum(Lkw_R1) + mean(Lkw_R2)))
daily_summary$Day = factor(x = daily_summary$Datum, levels = levels(as.factor(daily_summary$Datum)), labels = seq(1:365))

ggplot(daily_summary, aes(x=as.numeric(Day), y = count)) +
  geom_line(size = 1) + 
  xlab("Day") + ylab("AADT_t") + ylim(3000,6000)
