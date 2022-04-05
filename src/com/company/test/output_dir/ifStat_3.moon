entry
addi   r14,r0,topaddr  % Set stack pointer
main
addi r1,r0,4
sw mainx(r0),r1
lw r1,mainx(r0)
clti r2,r1,5
bz r2,block2
lw r1,x(r0)
putc r1
j endif1
block2
endif1
hlt
mainx      res 4
buf      res 40
