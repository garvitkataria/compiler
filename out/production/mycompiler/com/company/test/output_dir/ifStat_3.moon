
main []
generateStatementBlockCode
integer n1
integer n2
n1 = 7
n2 = 209
start generateIfStatementCode
start generateRelExprCode
integer temp0
temp0 = n2 + 9
integer temp1
temp1 = temp0 / 2
integer temp2
temp2 = n1 - 6
integer temp3
temp3 = temp2 * 100
condition# temp3 >= temp1
end generateRelExprCode 
start block 1
generateStatementBlockCode
n = 8
 ====generateStatementBlockCode==== 
end block 1
start block 2
generateStatementBlockCode
n = 2
 ====generateStatementBlockCode==== 
end block 2
end generateIfStatementCode
write n
 ====generateStatementBlockCode==== 
