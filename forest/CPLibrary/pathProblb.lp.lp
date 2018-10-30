\ENCODING=ISO-8859-1
\Problem name: CompactDef

Maximize
 obj: 10 PathValue102 + 10 PathValue103 + 10 PathValue104 + 10 PathValue105
      + 10 PathValue106
Subject To
 SumResources: PathValue102 + PathValue103 + PathValue104 + PathValue105
               + PathValue106  = 4
Bounds
 0 <= PathValue102 <= 1
 0 <= PathValue103 <= 1
 0 <= PathValue104 <= 1
 0 <= PathValue105 <= 1
 0 <= PathValue106 <= 1
End
