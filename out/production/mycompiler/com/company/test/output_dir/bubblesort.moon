entry
addi r1,r0,64
sw arr[0](r0),r1
addi r1,r0,34
sw arr[1](r0),r1
addi r1,r0,25
sw arr[2](r0),r1
addi r1,r0,12
sw arr[3](r0),r1
addi r1,r0,22
sw arr[4](r0),r1
addi r1,r0,11
sw arr[5](r0),r1
addi r1,r0,90
sw arr[6](r0),r1
lw r1,7(r0)
sw printarrayp1(r0),r1
lw r2,arr(r0)
sw printarrayp2(r0),r2
jl r15,printarray
printArraysize res 4
printArrayarr res 4
lw r1,size(r0)
sw n(r0),r1
addi r1,r0,0
sw i(r0),r1
gowhile1
lw r1,i(r0)
lw r2,n(r0)
clt r3,r1,r2
bz r3,endwhile1
addi r1,r0,0
muli r2,r1,0
lw r1,arr(r2)
putc r1
lw r1,i(r0)
addi r2,r1,1
sw temp0(r0),r2
lw r1,temp0(r0)
sw i(r0),r1
j gowhile1
endwhile1
bubbleSortsize res 4
bubbleSortarr res 4
lw r1,size(r0)
sw n(r0),r1
addi r1,r0,0
sw i(r0),r1
addi r1,r0,0
sw j(r0),r1
addi r1,r0,0
sw temp(r0),r1
lw r1,n(r0)
subi r2,r1,1
sw temp1(r0),r2
gowhile1
lw r1,i(r0)
lw r2,temp1(r0)
clt r3,r1,r2
bz r3,endwhile1
lw r1,n(r0)
lw r2,i(r0)
sub r3,r1,r2
sw temp2(r0),r3
lw r1,temp2(r0)
subi r2,r1,1
sw temp3(r0),r2
gowhile1
lw r1,j(r0)
lw r2,temp3(r0)
clt r3,r1,r2
bz r3,endwhile1
lw r1,arr[j](r0)
lw r2,arr(r0)
cgt r3,r1,r2
bz r3,block2
lw r1,arr[j](r0)
sw temp(r0),r1
lw r1,arr(r0)
sw arr[j](r0),r1
lw r1,temp(r0)
sw arr(r0),r1
j endif1
block2
endif1
lw r1,j(r0)
addi r2,r1,1
sw temp4(r0),r2
lw r1,temp4(r0)
sw j(r0),r1
j gowhile1
endwhile1
j gowhile1
endwhile1
lw r1,i(r0)
addi r2,r1,1
sw temp5(r0),r2
lw r1,temp5(r0)
sw i(r0),r1
hlt
temp      res 4
temp4      res 4
temp5      res 4
arr,integer,0      res 4
i      res 4
j      res 4
temp2      res 4
temp3      res 4
n      res 4
temp0      res 4
temp1      res 4
