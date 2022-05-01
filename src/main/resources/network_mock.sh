#! /bin/bash
max_count=$1
rabbitmq_host=$2

function random(){
  min=$1
  max=$(($2-$min+1))
  num=$(date +%s%N)
  echo $(($num%$max+$min))
}

for i in $( seq 1 $max_count)
do
  echo "$i -->> " & date & iptables -I INPUT -s $rabbitmq_host -j DROP
  iptables -I OUTPUT -s $rabbitmq_host -j DROP
#  sleep "$( random 1 10 )s"
  sleep 20s
  iptables -D INPUT 1
  iptables -D OUTPUT 1
  sleep 30s
done
