\ENCODING=ISO-8859-1
\Problem name: CompactDef

Maximize
 obj: 12.5 PathValue102 + 12.5 PathValue103 + 12.5 PathValue105
      + 12.5 PathValue106
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
