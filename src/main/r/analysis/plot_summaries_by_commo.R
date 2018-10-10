#PRE-CARRIAGE

# mode_year_commo = matrices %>%
#   group_by(year, commo = GuetergruppeVL, mode = VerkArtVL) %>% 
#   summarize(ton = sum(TonnenVL), ton_km = sum(TkmVL), count = n())
# 
# 
# 
# mode_year_commo = mode_year_commo %>% filter(!is.na(mode))
# 
# ggplot(mode_year_commo, aes(x=as.factor(year), y=ton/1e6,  fill = as.factor(commo))) + 
#   geom_bar(stat = "identity", position = "dodge", color = "white") + 
#   facet_grid(.~mode, scales = "free") + 
#   theme(legend.position = "bottom") + 
#   ggtitle("Pre-carriage by mode, year and commodity") + 
#   xlab("Year") + ylab("Million tn")
# 
# ggplot(mode_year_commo, aes(x=as.factor(year), y=ton_km/1e6,  fill = as.factor(commo))) + 
#   geom_bar(stat = "identity", position = "dodge", color = "white") + 
#   facet_grid(.~mode, scales = "free") + 
#   theme(legend.position = "bottom") + 
#   ggtitle("Pre-carriage by mode, year and commodity") + 
#   xlab("Year") + ylab("Million tn km")


#CARRIAGE

matrices_copy = merge(x=matrices, by.x = "GuetergruppeHL", y = commodity_groups, by.y = "Commodity")

mode_year_commo = matrices_copy %>%
  group_by(year, commo = Group, mode = ModeHL) %>% 
  summarize(count = n(), ton = sum(TonnenHL), ton_km = sum(TkmHL))

ggplot(mode_year_commo, aes(x=as.factor(year), y=ton/1e6,  fill = as.factor(commo))) + 
  geom_bar(stat = "identity", position = "dodge", color = "white") + 
  facet_grid(.~mode, scales = "free") + 
  theme(legend.position = "bottom") + 
  ggtitle("Main run by mode, year and commodity (grouped)") + 
  xlab("Year") + ylab("Million tn")

ggplot(mode_year_commo, aes(x=as.factor(year), y=ton_km/1e6,  fill = as.factor(commo))) + 
  geom_bar(stat = "identity", position = "dodge", color = "white") + 
  facet_grid(.~mode, scales = "free") + 
  theme(legend.position = "bottom") + 
  ggtitle("Main run by mode, year and commodity (grouped)") + 
  xlab("Year") + ylab("Million tn km")


#ON-CARRIAGE

# mode_year_commo = matrices %>%
#   group_by(year, commo = GuetergruppeNL, mode = VerkArtNL) %>% 
#   summarize(ton = sum(TonnenNL), ton_km = sum(TkmNL))
# 
# commodity_lab = commodities$Bezeichnung
# commodity_code = commodities$NST.2007
# 
# mode_lab = c("Empty", "Rail", "Road", "Water")
# mode_code = c(0, 1, 2, 3)
# 
# mode_year_commo$commo = factor(mode_year_commo$commo,
#                                levels = commodity_code,
#                                labels = commodity_lab)
# mode_year_commo$mode = factor(mode_year_commo$mode,
#                               levels = mode_code,
#                               labels = mode_lab)
# 
# mode_year_commo = mode_year_commo %>% filter(!is.na(mode))
# 
# ggplot(mode_year_commo, aes(x=as.factor(year), y=ton/1e6,  fill = as.factor(commo))) + 
#   geom_bar(stat = "identity", position = "dodge", color = "white") + 
#   facet_grid(.~mode, scales = "free") + 
#   theme(legend.position = "bottom") + 
#   ggtitle("On-carriage by mode, year and commodity") + 
#   xlab("Year") + ylab("Million tn")
# 
# ggplot(mode_year_commo, aes(x=as.factor(year), y=ton_km/1e6,  fill = as.factor(commo))) + 
#   geom_bar(stat = "identity", position = "dodge", color = "white") + 
#   facet_grid(.~mode, scales = "free") + 
#   theme(legend.position = "bottom") + 
#   ggtitle("On-carriage by mode, year and commodity") + 
#   xlab("Year") + ylab("Million tn km")

