% A = pack(av, n, m)
% pack vactor 'av' into a nxm matrix 'A' using acolumn concatenation
function A = packcolume(av, n, m)
for i = 1:m
  A(:,i) = av( ((i-1)*n+1) : (i*n) ) ;
end

