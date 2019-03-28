#analyze hourly distribution of trucks

avg_by_land = motorway_counts %>%
  group_by(Land, Stunde) %>% 
  summarize(count = mean(H1) + mean(H2))


ggplot(motorway_counts, aes(x=Stunde, y = H1 + H2, group = Zst, color = as.factor(Zst))) + 
  geom_line(alpha = .1) + theme(legend.position = "none")


ggplot(avg_by_land, aes(x=Stunde, y = count, group = Land, color = as.factor(Land))) + 
  geom_line(size = 1) +
  scale_color_discrete("") + 
  xlab("Time of day (hour)") + 
  ylab("Average truck volume (truck/hour)") + 
  scale_x_continuous(limits = c(1,23), expand = c(0,0)) + 
  scale_y_continuous(expand = c(0,0)) +
  theme_bw() + 
  theme(plot.margin = margin(.5,1,.5, .5, "cm"))


avg_hours = motorway_counts %>%
  group_by(Stunde) %>% 
  summarize(count = mean(H1) + mean(H2))

sum_hourly = sum(avg_hours$count)

ggplot(avg_hours, aes(x=Stunde, y = count/sum_hourly)) + 
  geom_line(size = 1) +
  scale_color_discrete("") + 
  xlab("Time of day (hour)") + 
  ylab("Average truck volume (relative to trucks/day") + 
  scale_x_continuous(limits = c(1,23), expand = c(0,0)) + 
  scale_y_continuous(limits = c(0,0.10), expand = c(0,0)) +
  theme_bw() + 
  theme(plot.margin = margin(.5,1,.5, .5, "cm"))


