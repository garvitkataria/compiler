
main [] : void
start generateStatementBlockCode
integer num
integer flag
flag = 0
integer i
i = 2
generateWhileStatementCode
start generateRelExprCode
integer temp0
temp0 = num / 2
condition# i <= temp0
end generateRelExprCode 
start generateStatementBlockCode
start generateIfStatementCode
start generateRelExprCode
integer temp1
temp1 = num / i
condition# temp1 == 0
end generateRelExprCode 
start block 1
start generateStatementBlockCode
flag = 1
end generateStatementBlockCode
end block 1
start block 2
start generateStatementBlockCode
end generateStatementBlockCode
end block 2
end generateIfStatementCode
integer temp2
temp2 = i + 1
i = temp2
end generateStatementBlockCode
====generateWhileStatementCode====
write flag
end generateStatementBlockCode
