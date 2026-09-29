    @Composable
    private fun AIChat(){
        val scope=rememberCoroutineScope()
        var input by remember{mutableStateOf("")}
        var loading by remember{mutableStateOf(false)}
        var messages by remember{mutableStateOf(listOf(AIMessage(false,"שלום. אפשר לשאול אותי שאלות או לבקש להוסיף אפליקציה או משחק.")))}

        Column(Modifier.fillMaxSize()){
            Row(
                Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=12.dp,vertical=8.dp),
                verticalAlignment=Alignment.CenterVertically
            ){
                Button(onClick={finish()}){Text("סגור")}
                Spacer(Modifier.width(8.dp))
                Text("צ׳אט AI",Modifier.weight(1f),textAlign=TextAlign.Right,style=MaterialTheme.typography.headlineMedium)
            }
            HorizontalDivider()
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal=12.dp),
                contentPadding=PaddingValues(top=8.dp,bottom=8.dp),
                verticalArrangement=Arrangement.spacedBy(6.dp)
            ){
                items(messages){m->
                    Card(Modifier.fillMaxWidth()){
                        Text(m.text,Modifier.fillMaxWidth().padding(12.dp),textAlign=TextAlign.Right)
                    }
                }
            }
            Surface(tonalElevation=3.dp){
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(8.dp),
                    verticalAlignment=Alignment.Bottom
                ){
                    OutlinedTextField(
                        value=input,
                        onValueChange={input=it},
                        modifier=Modifier.weight(1f),
                        label={Text("כתוב בקשה")},
                        maxLines=4
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        enabled=input.isNotBlank()&&!loading,
                        onClick={
                            val q=input.trim()
                            input=""
                            val sent=messages+AIMessage(true,q)
                            messages=sent
                            loading=true
                            scope.launch{
                                runCatching{askGroq(sent)}
                                    .onSuccess{messages=sent+AIMessage(false,handleAdd(it))}
                                    .onFailure{messages=sent+AIMessage(false,"לא הצלחתי להתחבר לשרת ה-AI.")}
                                loading=false
                            }
                        }
                    ){Text(if(loading)"..." else "שלח")}
                }
            }
        }
    }
}\n}\n