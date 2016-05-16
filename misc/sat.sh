sat()
{
    echo -n "\"$1\": "
    java -jar ./cli-1.0.jar "$1"
}

sat "c and -d"
sat "a and b or (c and -d)"
sat "a and (q then r iff b) or (-c and -d)"
sat "a and b and c and d and e and f and g and h and i and j and k and l and m and n and o and p and q and r and s and t and u and v and w and x and y and z and -o"
