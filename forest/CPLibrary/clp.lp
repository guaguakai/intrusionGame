\ENCODING=ISO-8859-1
\Problem name: CoreLP

Maximize
 obj: d
Subject To
 SumX: x1 + x2 + x3 + x4  = 1
 AP1:  d + 30 x1 + 30 x3 + 30 x4 <= 0
 AP2:  d + 30 x1 + 30 x2 + 30 x4 <= 0
 AP3:  d + 30 x1 + 30 x2 + 30 x3 <= 0
 AP4:  d + 30 x1 + 30 x2 + 30 x3 + 30 x4 <= 0
Bounds
-10000 <= d <= 10000
 0 <= x1 <= 1
 0 <= x2 <= 1
 0 <= x3 <= 1
 0 <= x4 <= 1
End
