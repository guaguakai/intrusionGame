\ENCODING=ISO-8859-1
\Problem name: CompactDef

Maximize
 obj: 0 Index + 16.6666666666667 PathValue4,0 + 16.6666666666667 PathValue8,1
      + 16.6666666666667 PathValue12,2
Subject To
 TotalValue4,0:              - IP(0,0) - IP(0,1) - IP(0,2) - IP(0,3)
                             + PathValue4,0 <= 0
 TotalValue8,1:              - IP(0,1) - IP(1,1) - IP(1,2) - IP(1,3)
                             + PathValue8,1 <= 0
 TotalValue12,2:             - IP(0,2) - IP(1,2) - IP(2,2) - IP(2,3)
                             + PathValue12,2 <= 0
 TotalValue16,3:             - IP(0,3) - IP(1,3) - IP(2,3) - IP(3,3)
                             + PathValue16,3 <= 0
 IntersectionValue(0,0):     - IP(0,0)1,1 - IP(0,0)4,1 + IP(0,0) <= 0
 KIntersectionValue(0,0)1,1: - Ip(0,0)1,1 + IP(0,0)1,1 <= 0
 KIntersectionValue(0,0)4,1: - Ip(0,0)4,1 + IP(0,0)4,1 <= 0
 IntersectionValue(0,1):     - IP(0,1)1,1 - IP(0,1)4,1 + IP(0,1) <= 0
 KIntersectionValue(0,1)1,1: - Ip(0,1)1,1 + IP(0,1)1,1 <= 0
 KIntersectionValue(0,1)4,1: - Ip(0,1)4,1 + IP(0,1)4,1 <= 0
 IntersectionValue(0,2):     - IP(0,2)1,1 - IP(0,2)4,1 + IP(0,2) <= 0
 KIntersectionValue(0,2)1,1: - Ip(0,2)1,1 + IP(0,2)1,1 <= 0
 KIntersectionValue(0,2)4,1: - Ip(0,2)4,1 + IP(0,2)4,1 <= 0
 IntersectionValue(0,3):     - IP(0,3)1,1 - IP(0,3)4,1 + IP(0,3) <= 0
 KIntersectionValue(0,3)1,1: - Ip(0,3)1,1 + IP(0,3)1,1 <= 0
 KIntersectionValue(0,3)4,1: - Ip(0,3)4,1 + IP(0,3)4,1 <= 0
 IntersectionValue(1,1):     - IP(1,1)1,1 - IP(1,1)4,1 + IP(1,1) <= 0
 KIntersectionValue(1,1)1,1: - Ip(1,1)1,1 + IP(1,1)1,1 <= 0
 KIntersectionValue(1,1)4,1: - Ip(1,1)4,1 + IP(1,1)4,1 <= 0
 IntersectionValue(1,2):     - IP(1,2)1,1 - IP(1,2)4,1 + IP(1,2) <= 0
 KIntersectionValue(1,2)1,1: - Ip(1,2)1,1 + IP(1,2)1,1 <= 0
 KIntersectionValue(1,2)4,1: - Ip(1,2)4,1 + IP(1,2)4,1 <= 0
 IntersectionValue(1,3):     - IP(1,3)1,1 - IP(1,3)4,1 + IP(1,3) <= 0
 KIntersectionValue(1,3)1,1: - Ip(1,3)1,1 + IP(1,3)1,1 <= 0
 KIntersectionValue(1,3)4,1: - Ip(1,3)4,1 + IP(1,3)4,1 <= 0
 IntersectionValue(2,2):     - IP(2,2)1,1 - IP(2,2)4,1 + IP(2,2) <= 0
 KIntersectionValue(2,2)1,1: - Ip(2,2)1,1 + IP(2,2)1,1 <= 0
 KIntersectionValue(2,2)4,1: - Ip(2,2)4,1 + IP(2,2)4,1 <= 0
 IntersectionValue(2,3):     - IP(2,3)1,1 - IP(2,3)4,1 + IP(2,3) <= 0
 KIntersectionValue(2,3)1,1: - Ip(2,3)1,1 + IP(2,3)1,1 <= 0
 KIntersectionValue(2,3)4,1: - Ip(2,3)4,1 + IP(2,3)4,1 <= 0
 IntersectionValue(3,3):     - IP(3,3)1,1 - IP(3,3)4,1 + IP(3,3) <= 0
 KIntersectionValue(3,3)1,1: - Ip(3,3)1,1 + IP(3,3)1,1 <= 0
 KIntersectionValue(3,3)4,1: - Ip(3,3)4,1 + IP(3,3)4,1 <= 0
 SumResources1,1:            Ip(0,0)1,1 + Ip(0,1)1,1 + Ip(0,2)1,1 + Ip(0,3)1,1
                             + Ip(1,1)1,1 + Ip(1,2)1,1 + Ip(1,3)1,1
                             + Ip(2,2)1,1 + Ip(2,3)1,1 + Ip(3,3)1,1  = 1
 SumResources4,1:            Ip(0,0)4,1 + Ip(0,1)4,1 + Ip(0,2)4,1 + Ip(0,3)4,1
                             + Ip(1,1)4,1 + Ip(1,2)4,1 + Ip(1,3)4,1
                             + Ip(2,2)4,1 + Ip(2,3)4,1 + Ip(3,3)4,1  = 1
Bounds
 0 <= Ip(0,0)1,1 <= 1
 0 <= IP(0,0)1,1 <= 1
 0 <= Ip(0,0)4,1 <= 1
 0 <= IP(0,0)4,1 <= 1
 0 <= IP(0,0) <= 1
      Ip(0,1)1,1 = 0
      IP(0,1)1,1 = 0
 0 <= Ip(0,1)4,1 <= 1
 0 <= IP(0,1)4,1 <= 1
 0 <= IP(0,1) <= 1
      Ip(0,2)1,1 = 0
      IP(0,2)1,1 = 0
      Ip(0,2)4,1 = 0
      IP(0,2)4,1 = 0
 0 <= IP(0,2) <= 1
      Ip(0,3)1,1 = 0
      IP(0,3)1,1 = 0
      Ip(0,3)4,1 = 0
      IP(0,3)4,1 = 0
 0 <= IP(0,3) <= 1
 0 <= Ip(1,1)1,1 <= 1
 0 <= IP(1,1)1,1 <= 1
 0 <= Ip(1,1)4,1 <= 1
 0 <= IP(1,1)4,1 <= 1
 0 <= IP(1,1) <= 1
      Ip(1,2)1,1 = 0
      IP(1,2)1,1 = 0
 0 <= Ip(1,2)4,1 <= 1
 0 <= IP(1,2)4,1 <= 1
 0 <= IP(1,2) <= 1
      Ip(1,3)1,1 = 0
      IP(1,3)1,1 = 0
      Ip(1,3)4,1 = 0
      IP(1,3)4,1 = 0
 0 <= IP(1,3) <= 1
 0 <= Ip(2,2)1,1 <= 1
 0 <= IP(2,2)1,1 <= 1
 0 <= Ip(2,2)4,1 <= 1
 0 <= IP(2,2)4,1 <= 1
 0 <= IP(2,2) <= 1
      Ip(2,3)1,1 = 0
      IP(2,3)1,1 = 0
 0 <= Ip(2,3)4,1 <= 1
 0 <= IP(2,3)4,1 <= 1
 0 <= IP(2,3) <= 1
 0 <= Ip(3,3)1,1 <= 1
 0 <= IP(3,3)1,1 <= 1
 0 <= Ip(3,3)4,1 <= 1
 0 <= IP(3,3)4,1 <= 1
 0 <= IP(3,3) <= 1
      Index = 0
 0 <= PathValue4,0 <= 1
 0 <= PathValue8,1 <= 1
 0 <= PathValue12,2 <= 1
 0 <= PathValue16,3 <= 1
Generals
 Ip(0,0)1,1  IP(0,0)1,1  Ip(0,0)4,1  IP(0,0)4,1  Ip(0,1)1,1  IP(0,1)1,1 
 Ip(0,1)4,1  IP(0,1)4,1  Ip(0,2)1,1  IP(0,2)1,1  Ip(0,2)4,1  IP(0,2)4,1 
 Ip(0,3)1,1  IP(0,3)1,1  Ip(0,3)4,1  IP(0,3)4,1  Ip(1,1)1,1  IP(1,1)1,1 
 Ip(1,1)4,1  IP(1,1)4,1  Ip(1,2)1,1  IP(1,2)1,1  Ip(1,2)4,1  IP(1,2)4,1 
 Ip(1,3)1,1  IP(1,3)1,1  Ip(1,3)4,1  IP(1,3)4,1  Ip(2,2)1,1  IP(2,2)1,1 
 Ip(2,2)4,1  IP(2,2)4,1  Ip(2,3)1,1  IP(2,3)1,1  Ip(2,3)4,1  IP(2,3)4,1 
 Ip(3,3)1,1  IP(3,3)1,1  Ip(3,3)4,1  IP(3,3)4,1 
End
