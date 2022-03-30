
main []
generateStatementBlockCode
integer arr[7]
arr[0] = 64
arr[1] = 34
arr[2] = 25
arr[3] = 12
arr[4] = 22
arr[5] = 11
arr[6] = 90
printarray [7, arr[7]]
 ====generateStatementBlockCode==== 

printArray [integer size, integer arr[7]]
generateStatementBlockCode
integer n
integer i
n = size
i = 0
generateWhileStatementCode
generateRelExprCode
i < n
====generateRelExprCode====
generateStatementBlockCode
write arr[i]
integer temp0
temp0 = i + 1
i = temp0
 ====generateStatementBlockCode==== 
====generateWhileStatementCode====
 ====generateStatementBlockCode==== 
