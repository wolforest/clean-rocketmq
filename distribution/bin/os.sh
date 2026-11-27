#!/bin/sh

# Tune the operating system for the wolfmq store (mmap based commitLog).
# Linux only, run once as root:
#   sudo sh bin/os.sh
#
# The values are reference values tuned for the mmap based store, adjust them
# for the host and the workload. Rebooting applies the persisted values.
#
# What it does, idempotent (running twice writes nothing twice):
#   - kernel settings for the mmap commitLog and page cache write back,
#     written to /etc/sysctl.d/99-wolfmq.conf and applied now
#   - open file limits, written to /etc/security/limits.conf and
#     /etc/profile.d/wolfmq.sh
#
# It does not touch io scheduler and memlock of the rocketmq script:
# modern NVMe disks use the none scheduler and wolfmq does not lock memory.

error_exit ()
{
    echo "ERROR: $1 !!" >&2
    exit 1
}

case "`uname`" in
    Linux) ;;
    *) error_exit "os.sh is for Linux, this is `uname`" ;;
esac

[ "$(id -u)" = "0" ] || error_exit "run os.sh as root"

SYSCTL_CONF=/etc/sysctl.d/99-wolfmq.conf
LIMITS_CONF=/etc/security/limits.conf

#===========================================================================================
# kernel settings
#===========================================================================================
if [ -f "$SYSCTL_CONF" ]; then
    echo "$SYSCTL_CONF exists, do nothing."
else
    cat > "$SYSCTL_CONF" <<'EOF'
# added by wolfmq os.sh, reference values for the mmap based store
vm.overcommit_memory = 1
vm.max_map_count = 655360
vm.swappiness = 1
vm.zone_reclaim_mode = 0
vm.dirty_background_ratio = 50
vm.dirty_ratio = 50
vm.dirty_writeback_centisecs = 360000
vm.page_cluster = 3
EOF
    echo "create $SYSCTL_CONF ok"
fi

sysctl -p "$SYSCTL_CONF" || echo "WARN: some kernel settings could not be applied"

#===========================================================================================
# open file limits
#===========================================================================================
if grep -q "added by wolfmq os.sh" "$LIMITS_CONF" 2>/dev/null; then
    echo "limits already in $LIMITS_CONF, do nothing."
else
    cat >> "$LIMITS_CONF" <<'EOF'
# added by wolfmq os.sh
* soft nofile 655350
* hard nofile 655350
EOF
    echo "append nofile limits to $LIMITS_CONF ok"
fi

if [ -f /etc/profile.d/wolfmq.sh ]; then
    echo "/etc/profile.d/wolfmq.sh exists, do nothing."
else
    cat > /etc/profile.d/wolfmq.sh <<'EOF'
ulimit -n 655350
EOF
    echo "create /etc/profile.d/wolfmq.sh ok"
fi

#===========================================================================================
# summary
#===========================================================================================
echo "---------------------------------------------------------------"
sysctl vm.overcommit_memory vm.max_map_count vm.swappiness vm.zone_reclaim_mode
sysctl vm.dirty_background_ratio vm.dirty_ratio
sysctl vm.dirty_writeback_centisecs vm.page_cluster
echo "ulimit -n: `ulimit -n`"
echo "os tuning done, a login shell is required for the new open file limits."
