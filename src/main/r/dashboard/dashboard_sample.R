pacman::p_load(data.table, ggplot2, dplyr, reshape, shiny, shinydashboard, plotly)



folder = "c:/models/freightFlows/"
source("c:/code/freightFlows/src/main/r/input/input_zones.R")
source("c:/code/freightFlows/src/main/r/input/input_commodities.R")
source("c:/code/freightFlows/src/main/r/input/input_matrices.R")
#need to clean up variables!

zones_aux = zones %>% select(orig = Verkehrszelle, origType = type)
matrices = merge(x=matrices, y=zones_aux, by.x= "Quellzelle", by.y = "orig")
zones_aux = zones %>% select(dest = Verkehrszelle, destType = type)
matrices = merge(x=matrices, y=zones_aux, by.x= "Zielzelle", by.y = "dest")


ui <- dashboardPage(
  dashboardHeader(),
  dashboardSidebar(
    checkboxInput("allOrigin", "all origins", T),
    selectInput("origZone", "origin", multiple = T, choices = zones$Verkehrszellenname ),
    checkboxInput("allDestination", "all destinations", T),
    selectInput("destZone", "destination", multiple = T, choices = zones$Verkehrszellenname )
  ),
  dashboardBody(
    # Boxes need to be put in a row (or column)
    fluidRow(
      box(plotlyOutput("plot1", height = 900)),
      box(plotlyOutput("plot2", height = 900))
    )
  )
)

server <- function(input, output) {
  
  
  
  
  output$plot1 <- renderPlotly({
    
    filtered = filterData(matrices, input)
    
    thisData = filtered %>% 
      group_by(year, mode = ModeHL) %>%
      summarize(ton = sum(as.numeric(TonnenHL))/1e6)
    
    #removes empty from the levels of mode
    thisData$mode = factor(thisData$mode, levels = mode_lab[2:4], labels = mode_lab[2:4])
    
    ggplot(thisData, aes(x= as.factor(year), y = ton, fill = mode)) + 
      geom_bar(stat = "identity", position = position_dodge(preserve = "single")) + 
      scale_fill_manual(values = c("#ff6c73", "#4c4c4c", "#6fb5f6"), drop = F) +
      ggtitle("Volume in tons") + 
      theme(legend.position = "bottom")
   
    
  })
  
  output$plot2 = renderPlotly({
    
    filtered = filterData(matrices, input)
    
    thisData = filtered %>% 
      group_by(year, commo = GuetergruppeHL) %>%
      summarize(ton = sum(as.numeric(TonnenHL))/1e6)
    
    ggplot(thisData, aes(x= as.factor(year), y = ton, fill = commo)) + 
      geom_bar(stat = "identity", position = position_dodge(preserve = "single")) + 
      ggtitle("Volume in tons")  + 
      theme(legend.position = "bottom", legend.key.width = unit(10, "points"))
  })
}

filterData = function(matrices, input, output){
  if(!input$allOrigin){
    origId = zones %>% filter(Verkehrszellenname %in% input$origZone) %>% select(Verkehrszelle)
    filtered = matrices %>%
      filter(Quellzelle %in% origId$Verkehrszelle)
  } else {
    filtered = matrices
  }
  
  if (!input$allDestination){
    destId = zones %>% filter(Verkehrszellenname %in% input$destZone) %>% select(Verkehrszelle)
    filtered = filtered %>%
      filter(Zielzelle %in% destId$Verkehrszelle)
  } else {
  }
  return(filtered)
}

shinyApp(ui, server)
