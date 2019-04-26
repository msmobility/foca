
#counts zone by type
zones %>% group_by(type) %>% summarise(n())

#summmarize by mode and zone type: 

zones_aux = zones %>% select(orig = Verkehrszelle, origType = type)
matrices = merge(x=matrices, y=zones_aux, by.x= "Quellzelle", by.y = "orig")
zones_aux = zones %>% select(dest = Verkehrszelle, destType = type)
matrices = merge(x=matrices, y=zones_aux, by.x= "Zielzelle", by.y = "dest")



#main run

zone_case = list(zones$Verkehrszelle, 9162, 9362)
case = c("all", "Munich", "Regensburg")

for (i in 1:3){

  summary = matrices %>%
    filter(Zielzelle %in% zone_case[[i]]) %>% 
    group_by(year, mode = ModeHL, origType, destType) %>%
    summarize(ton = sum(as.numeric(TonnenHL))/1e6)
  
  p1 = ggplot(summary, aes(x= as.factor(year), y = ton, fill = mode)) + 
    geom_bar(stat = "identity", position = position_dodge(preserve = "single")) + 
    facet_grid(origType ~ destType) + 
    scale_fill_manual(values = c("#ff6c73", "#4c4c4c", "#6fb5f6")) +
    ggtitle(paste("Flows by origCoord and destCoord - to ",case[i],sep = ""))
  
  print(p1)
  
}

# #pre run
# 
# summary = matrices %>% group_by(year, mode = ModeVL, origType, destType) %>% summarize(ton = sum(TonnenVL))
# 
# summary$mode= factor(summary$mode, levels = mode_code, labels = mode_lab)
# 
# 
# ggplot(summary, aes(x= as.factor(year), y = ton, fill = mode)) + 
#   geom_bar(stat = "identity", position = "dodge2") + 
#   facet_grid(origType ~ destType)
# 
# #on-carriage
# 
# summary = matrices %>% group_by(year, mode = ModeNL, origType, destType) %>% summarize(ton = sum(TonnenNL))
# 
# summary$mode= factor(summary$mode, levels = mode_code, labels = mode_lab)
# 
# 
# ggplot(summary, aes(x= as.factor(year), y = ton, fill = mode)) + 
#   geom_bar(stat = "identity", position = "dodge2") + 
#   facet_grid(origType ~ destType)
# 


#plot flows by zone type


summary = matrices %>%
  group_by(year, mode = ModeHL, origType, destType) %>%
  summarize(ton = sum(as.numeric(TonnenHL))/1e6)

p1 = ggplot(summary, aes(x= as.factor(year), y = ton, fill = mode)) + 
  geom_bar(stat = "identity", position = "fill") + 
  facet_grid(origType ~ destType) + 
  scale_fill_manual(values = c("#ff6c73", "#4c4c4c", "#6fb5f6")) +
  ggtitle(paste("Flows by origCoord and destCoord - to ",case[i],sep = ""))

print(p1)

summary %>% filter(mode == "Road")


