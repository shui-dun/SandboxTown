<template>
    <div>
        <NavGroup :items="tabs" @close="$emit('close')">
            <template v-slot:0>
                <EquipmentPannel />
            </template>
            <template v-slot:1>
                <BackpackPannel />
            </template>
        </NavGroup>
    </div>
</template>

<script>
import NavGroup from './NavGroup.vue';
import EquipmentPannel from './EquipmentPannel.vue';
import BackpackPannel from './BackpackPannel.vue';

export default {
    components: {
        NavGroup,
        EquipmentPannel,
        BackpackPannel,
    },
    data() {
        return {
            tabs: ['装备栏', '物品栏'],
        };
    },
}
</script>
