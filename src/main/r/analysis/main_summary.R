#plots modal shares of the entire area, to Munich and to Regensburg

matrices_copy = matrices

zone_case = list(zones$Verkehrszelle, 9162, 9362)
case = c("all", "Munich", "Regensburg")

for (i in 1:3){
  
  modal_shares = matrices_copy %>%
    filter(Zielzelle %in% zone_case[[i]]) %>%
    group_by(year, mode = ModeHL) %>%
    summarize(tons = sum(as.numeric(TonnenHL))/1e6, flows = n())
  
  print(case[i])
  print(modal_shares)
  
  p1 = ggplot(modal_shares, aes(x=year, y = tons, fill = mode)) +
    geom_bar(stat = "identity", position = "fill") + 
    scale_fill_manual(values = c("#ff6c73", "#4c4c4c", "#6fb5f6")) + 
    ggtitle(paste("Mode by volume in tons to ", case[i], " zones", sep = "")) + 
              ylab("share")
  
  modal_shares_VL = matrices_copy %>%
    filter(Zielzelle %in% zone_case[[i]]) %>%
    group_by(year, mode = ModeVL) %>%
    summarize(tons = sum(as.numeric(TonnenVL))/1e6, flows = n())
  
  modal_shares_NL = matrices_copy %>%
    filter(Zielzelle %in% zone_case[[i]]) %>%
    group_by(year, mode = ModeNL) %>%
    summarize(tons = sum(as.numeric(TonnenNL))/1e6, flows = n())
  
  modal_shares$segment = "2:main"
  modal_shares_VL$segment = "1:pre-carriage"
  modal_shares_NL$segment = "3:on-carriage"
  modal_shares = rbind(modal_shares, modal_shares_VL, modal_shares_NL)  

  p2 = ggplot(modal_shares %>% filter(mode!= "Empty"), aes(x=segment, y = tons, fill = mode)) +
   geom_bar(stat = "identity", position = position_dodge(preserve = "single")) + 
   scale_fill_manual(values = c("#ff6c73", "#4c4c4c", "#6fb5f6")) + 
   ggtitle(paste("Mode by volume to ", case[i], " zones", sep = "")) + 
    facet_grid(.~year) + 
    ylab("tons (million)")
  
  plot(p1)
  plot(p2)
  rm(modal_shares, modal_shares_VL, modal_shares_NL)
}


summary(matrices_copy)
