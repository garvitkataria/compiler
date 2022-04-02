
main [] : void
start generateStatementBlockCode
integer arr[7]
arr[0] = 64
arr[1] = 34
arr[2] = 25
arr[3] = 12
arr[4] = 22
arr[5] = 11
arr[6] = 90
printarray [7, arr[7]]
end generateStatementBlockCode

printArray [integer size, integer arr[7]] : integer
start generateStatementBlockCode
integer n
integer i
n = size
i = 0
generateWhileStatementCode
start generateRelExprCode
condition# i < n
end generateRelExprCode 
start generateStatementBlockCode
write arr[i]
integer temp7
temp7 = i + 1
i = temp7
end generateStatementBlockCode
====generateWhileStatementCode====
return n
end generateStatementBlockCode
