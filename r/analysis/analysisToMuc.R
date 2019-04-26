

matrices %>%
  filter(Zielzelle == 9162, GuetergruppeHL == "Post, Pakete") %>%
  group_by(ModeHL, year, VerkArtHL) %>%
  summarize(volume = sum(TonnenHL)/365, volume_nl = sum(TonnenNL)/365)
