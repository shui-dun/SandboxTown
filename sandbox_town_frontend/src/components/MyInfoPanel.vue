<template>
    <div>
        <NavGroup :items="tabs" @close="$emit('close')">
            <template v-slot:0>
                <EquipmentPanel />
            </template>
            <template v-slot:1>
                <BackpackPanel />
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import EquipmentPanel from './EquipmentPanel.vue';
import BackpackPanel from './BackpackPanel.vue';

export default {
    components: {
        NavGroup,
        EquipmentPanel,
        BackpackPanel,
    },
    data() {
        return {
            tabs: ['装备栏', '物品栏'],
        };
    },
}
</script>
